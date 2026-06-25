const { PrismaClient } = require('@prisma/client');
const prisma = new PrismaClient();

async function main() {
  await prisma.user.upsert({
    where: { accessToken: 'pajaro' },
    update: {},
    create: {
      name: 'PAJARO',
      accessToken: 'pajaro',
    },
  });

  await prisma.user.upsert({
    where: { accessToken: 'roman' },
    update: {},
    create: {
      name: 'ROMAN',
      accessToken: 'roman',
    },
  });

  console.log('Seed finished: PAJARO and ROMAN created.');
}

main()
  .catch((e) => {
    console.error(e);
    process.exit(1);
  })
  .finally(async () => {
    await prisma.$disconnect();
  });
