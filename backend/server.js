const express = require('express');
const cookieParser = require('cookie-parser');
const axios = require('axios');
const { PrismaClient } = require('@prisma/client');

const prisma = new PrismaClient();
const app = express();
const PORT = process.env.PORT || 3000;

app.use(express.json());
app.use(cookieParser(process.env.SESSION_SECRET || 'fallback_secret'));

// Middleware de Autenticación
const auth = async (req, res, next) => {
  const userId = req.signedCookies.session_id;
  if (!userId) return res.status(401).send("No identificado");
  const user = await prisma.user.findUnique({ where: { id: userId } });
  if (!user) return res.status(401).send("Usuario no existe");
  req.user = user;
  next();
};

// 1. Link Mágico
app.get('/auth/:token', async (req, res) => {
  const user = await prisma.user.findUnique({ where: { accessToken: req.params.token } });
  if (!user) return res.status(404).send("Token inválido");

  res.cookie('session_id', user.id, {
    httpOnly: true,
    signed: true,
    maxAge: 30 * 24 * 60 * 60 * 1000, // 30 días
    sameSite: 'none',
    secure: true
  });
  res.send({ status: "ok", name: user.name });
});

// 2. Guardar Predicción
app.post('/predict', auth, async (req, res) => {
  const { matchId, predHome, predAway } = req.body;

  if (matchId === undefined || predHome === undefined || predAway === undefined) {
    return res.status(400).send("Faltan datos en el body");
  }

  const match = await prisma.match.findUnique({ where: { id: parseInt(matchId) } });

  if (!match) return res.status(404).send("Partido no encontrado en la base de datos");

  const now = new Date();
  const kickoff = new Date(match.kickoffUtc);
  const deadline = new Date(kickoff.getTime() - 10 * 60000);

  if (now > deadline) {
    return res.status(403).send(`Ventana cerrada. Límite era: ${deadline.toISOString()}`);
  }

  try {
    const pred = await prisma.prediction.upsert({
      where: { userId_matchId: { userId: req.user.id, matchId: parseInt(matchId) } },
      update: {},
      create: { userId: req.user.id, matchId: parseInt(matchId), predHome, predAway, state: 'LOCKED' }
    });
    res.json(pred);
  } catch (e) {
    console.error("Error en upsert prediction:", e);
    res.status(409).send("Ya tienes una predicción guardada para este juego");
  }
});

// 3. Obtener Partidos
app.get('/matches', auth, async (req, res) => {
  const matches = await prisma.match.findMany({
    orderBy: { kickoffUtc: 'asc' },
    include: { predictions: true }
  });

  const response = matches.map(m => {
    const myPred = m.predictions.find(p => p.userId === req.user.id);
    const rivalPred = m.predictions.find(p => p.userId !== req.user.id);

    return {
      ...m,
      myPrediction: myPred || null,
      rivalPrediction: m.status === 'FINISHED' ? rivalPred : (rivalPred ? { state: 'LOCKED' } : null)
    };
  });
  res.json(response);
});

// 4. Standings
app.get('/standings', async (req, res) => {
  const users = await prisma.user.findMany({
    include: { predictions: true }
  });

  const standings = users.map(u => {
    const scoredPreds = u.predictions.filter(p => p.state === 'SCORED');
    const points = scoredPreds.reduce((sum, p) => sum + p.points, 0);
    const exactAciertos = scoredPreds.filter(p => p.points === 1).length;
    return { name: u.name, points, exactAciertos };
  }).sort((a, b) => b.points - a.points || b.exactAciertos - a.exactAciertos);

  res.json(standings);
});

// 5. El Corazón: /tick
app.get('/tick', async (req, res) => {
  if (req.query.secret !== process.env.TICK_SECRET) return res.status(403).json({ok:false});

  try {
    // A. Refrescar Matches
    const footballRes = await axios.get('https://api.football-data.org/v4/competitions/WC/matches', {
      headers: { 'X-Auth-Token': process.env.FOOTBALL_DATA_TOKEN }
    });

    if (footballRes.data && footballRes.data.matches) {
      for (const apiMatch of footballRes.data.matches) {
        await prisma.match.upsert({
          where: { id: apiMatch.id },
          update: {
            status: apiMatch.status,
            homeScore: apiMatch.score.fullTime.home,
            awayScore: apiMatch.score.fullTime.away,
            homeTeam: apiMatch.homeTeam ? apiMatch.homeTeam.name : "TBD",
            awayTeam: apiMatch.awayTeam ? apiMatch.awayTeam.name : "TBD"
          },
          create: {
            id: apiMatch.id,
            kickoffUtc: new Date(apiMatch.utcDate),
            status: apiMatch.status,
            homeTeam: apiMatch.homeTeam ? apiMatch.homeTeam.name : "TBD",
            awayTeam: apiMatch.awayTeam ? apiMatch.awayTeam.name : "TBD",
            homeScore: apiMatch.score.fullTime.home,
            awayScore: apiMatch.score.fullTime.away,
            stage: apiMatch.stage
          }
        });
      }
    }

    // B. Evaluar Predicciones
    const finishedMatches = await prisma.match.findMany({ where: { status: 'FINISHED' } });
    for (const m of finishedMatches) {
      const pendingPreds = await prisma.prediction.findMany({
        where: { matchId: m.id, state: 'LOCKED' }
      });

      for (const p of pendingPreds) {
        let points = (p.predHome === m.homeScore && p.predAway === m.awayScore) ? 1 : 0;
        await prisma.prediction.update({
          where: { id: p.id },
          data: { points, state: 'SCORED' }
        });
      }
    }

    res.json({ok:true}); // Respuesta mínima en JSON
  } catch (e) {
    console.error(e);
    res.status(500).json({ok:false, error: "fail"});
  }
});

app.listen(PORT, () => console.log(`Server running on port ${PORT}`));
