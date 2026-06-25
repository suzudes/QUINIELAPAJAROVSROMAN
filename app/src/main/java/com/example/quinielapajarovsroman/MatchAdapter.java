package com.example.quinielapajarovsroman;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class MatchAdapter extends RecyclerView.Adapter<MatchAdapter.ViewHolder> {

    public interface OnMatchClickListener {
        void onSavePrediction(MatchEntity match, int home, int away);
    }

    private List<ClosedMatchWithPredictions> matches = new ArrayList<>();
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private final ZoneId cdmxZone = ZoneId.of("America/Mexico_City");
    private OnMatchClickListener listener;

    public void setMatches(List<ClosedMatchWithPredictions> matches) {
        this.matches = matches;
        notifyDataSetChanged();
    }

    public void setListener(OnMatchClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_match, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ClosedMatchWithPredictions item = matches.get(position);
        MatchEntity match = item.match;
        
        holder.homeTeamName.setText(match.homeTeam);
        holder.awayTeamName.setText(match.awayTeam);
        
        ZonedDateTime utcDateTime = ZonedDateTime.parse(match.utcDate);
        ZonedDateTime cdmxDateTime = utcDateTime.withZoneSameInstant(cdmxZone);
        holder.matchTimeText.setText(cdmxDateTime.format(timeFormatter) + " CDMX | " + match.matchDay);

        // Reset views
        holder.revealedLayout.setVisibility(View.GONE);
        holder.saveButton.setVisibility(View.GONE);
        holder.lockIcon.setVisibility(View.GONE);
        holder.homeScoreInput.setEnabled(true);
        holder.awayScoreInput.setEnabled(true);
        holder.homeScoreInput.setText("");
        holder.awayScoreInput.setText("");

        if ("FINISHED".equals(match.status)) {
            // Marcador Real
            holder.homeScoreInput.setText(String.valueOf(match.homeScore));
            holder.awayScoreInput.setText(String.valueOf(match.awayScore));
            holder.homeScoreInput.setEnabled(false);
            holder.awayScoreInput.setEnabled(false);
            holder.homeScoreInput.setTextColor(Color.parseColor("#C6FF00")); // Lima
            holder.awayScoreInput.setTextColor(Color.parseColor("#C6FF00"));
            
            // Revelar Predicciones
            holder.revealedLayout.setVisibility(View.VISIBLE);
            displayPredictions(holder, item.predictions);
            
        } else {
            // Partidos por jugar
            holder.homeScoreInput.setTextColor(Color.WHITE);
            holder.awayScoreInput.setTextColor(Color.WHITE);
            holder.homeScoreInput.setHint("-");
            holder.awayScoreInput.setHint("-");
            
            // Si hay predicción del usuario logueado (simulado por ahora con la primera que encuentre)
            if (item.predictions != null && !item.predictions.isEmpty()) {
                holder.lockIcon.setVisibility(View.VISIBLE);
                holder.homeScoreInput.setText(String.valueOf(item.predictions.get(0).predHome));
                holder.awayScoreInput.setText(String.valueOf(item.predictions.get(0).predAway));
                holder.homeScoreInput.setEnabled(false);
                holder.awayScoreInput.setEnabled(false);
            } else {
                holder.saveButton.setVisibility(View.VISIBLE);
                holder.saveButton.setOnClickListener(v -> {
                    String h = holder.homeScoreInput.getText().toString();
                    String a = holder.awayScoreInput.getText().toString();
                    if (!h.isEmpty() && !a.isEmpty() && listener != null) {
                        listener.onSavePrediction(match, Integer.parseInt(h), Integer.parseInt(a));
                    }
                });
            }
        }
    }

    private void displayPredictions(ViewHolder holder, List<PredictionEntity> predictions) {
        holder.userPredictionText.setText("PAJARO: SIN PREDICCIÓN");
        holder.rivalPredictionText.setText("ROMAN: SIN PREDICCIÓN");
        holder.userPredictionText.setTextColor(Color.GRAY);
        holder.rivalPredictionText.setTextColor(Color.GRAY);

        if (predictions == null) return;

        for (PredictionEntity pred : predictions) {
            String label = (pred.points == 1) ? " (ACIERTO +1)" : " (FALLO 0)";
            int color = (pred.points == 1) ? Color.parseColor("#C6FF00") : Color.RED;
            if ("CANCELLED".equals(pred.state)) {
                label = " (CANCELADO)";
                color = Color.GRAY;
            }

            String text = pred.userId + ": " + pred.predHome + " - " + pred.predAway + label;
            
            if ("PAJARO".equals(pred.userId)) {
                holder.userPredictionText.setText(text);
                holder.userPredictionText.setTextColor(color);
            } else if ("ROMAN".equals(pred.userId)) {
                holder.rivalPredictionText.setText(text);
                holder.rivalPredictionText.setTextColor(color);
            }
        }
    }

    @Override
    public int getItemCount() {
        return matches.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView homeTeamName, awayTeamName, matchTimeText;
        EditText homeScoreInput, awayScoreInput;
        Button saveButton;
        ImageView lockIcon;
        View revealedLayout;
        TextView userPredictionText, rivalPredictionText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            homeTeamName = itemView.findViewById(R.id.homeTeamName);
            awayTeamName = itemView.findViewById(R.id.awayTeamName);
            matchTimeText = itemView.findViewById(R.id.matchTimeText);
            homeScoreInput = itemView.findViewById(R.id.homeScoreInput);
            awayScoreInput = itemView.findViewById(R.id.awayScoreInput);
            saveButton = itemView.findViewById(R.id.saveButton);
            lockIcon = itemView.findViewById(R.id.lockIcon);
            revealedLayout = itemView.findViewById(R.id.revealedLayout);
            userPredictionText = itemView.findViewById(R.id.userPredictionText);
            rivalPredictionText = itemView.findViewById(R.id.rivalPredictionText);
        }
    }
}
