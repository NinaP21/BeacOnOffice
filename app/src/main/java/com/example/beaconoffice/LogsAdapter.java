package com.example.beaconoffice;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

/**
 * LogsAdapter class holds the data that will be shown in "Measurement Results" table in Logs page.
 * This class is responsible for matching every LogResult object with the "Measurement Results" table.
 *
 * @author Aikaterini - Maria Panteleaki
 * @version 1.0
 * @see LogResult
 * @since 31/8/2022
 */
public class LogsAdapter extends RecyclerView.Adapter<LogsAdapter.ViewHolder> {

    private ArrayList<LogResult> logResultsList;
    private Context context;

    public LogsAdapter (ArrayList<LogResult> logResultsList, Context context) {
        this.logResultsList = logResultsList;
        this.context = context;
    }

    @NonNull
    @Override
    public LogsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.logs_line, parent, false);
        LogsAdapter.ViewHolder viewHolder = new LogsAdapter.ViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull LogsAdapter.ViewHolder holder, int position) {
        String timestamp = logResultsList.get(position).getTimestamp();
        String xCoord = String.valueOf(logResultsList.get(position).getxCoord());
        String yCoord = String.valueOf(logResultsList.get(position).getyCoord());
        String distance1 = String.valueOf(logResultsList.get(position).getDistance1());
        String distance2 = String.valueOf(logResultsList.get(position).getDistance2());
        String distance3 = String.valueOf(logResultsList.get(position).getDistance3());
        holder.setData(timestamp, xCoord, yCoord, distance1, distance2, distance3);
        holder.bind(logResultsList.get(position));
    }

    @Override
    public int getItemCount() {
        return logResultsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView timestamp;
        private TextView xCoord;
        private TextView yCoord;
        private TextView distance1;
        private TextView distance2;
        private TextView distance3;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            timestamp = itemView.findViewById(R.id.timestamp);
            xCoord = itemView.findViewById(R.id.x);
            yCoord = itemView.findViewById(R.id.y);
            distance1 = itemView.findViewById(R.id.distance1);
            distance2 = itemView.findViewById(R.id.distance2);
            distance3 = itemView.findViewById(R.id.distance3);
        }

        public void setData(String timestamp, String x, String y, String distance1, String distance2, String distance3) {
            this.timestamp.setText(timestamp);
            xCoord.setText("x: " + x);
            yCoord.setText("y: " + y);
            this.distance1.setText(distance1);
            this.distance2.setText(distance2);
            this.distance3.setText(distance3);
        }

        public void bind(LogResult item) {
            timestamp.setText(item.getTimestamp());
            xCoord.setText("x: " + item.getxCoord());
            yCoord.setText("y: " + item.getyCoord());
            distance1.setText(item.getDistance1());
            distance2.setText(item.getDistance2());
            distance3.setText(item.getDistance3());
        }
    }
}
