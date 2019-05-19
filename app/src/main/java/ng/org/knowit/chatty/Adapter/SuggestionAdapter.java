package ng.org.knowit.chatty.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import ng.org.knowit.chatty.R;

public class SuggestionAdapter extends RecyclerView.Adapter<SuggestionAdapter.SuggestionViewHolder> {

    Context mContext;
    ArrayList<String> mStringList;
    private final OnListItemClickListener mOnListItemClickListener;

    public SuggestionAdapter(Context context, ArrayList<String> stringList, OnListItemClickListener onListItemClickListener){
        this.mContext = context;
        this.mStringList = stringList;
        this.mOnListItemClickListener = onListItemClickListener;
    }

    public interface OnListItemClickListener {
        void onListItemClick(int position);
    }

    @NonNull
    @Override
    public SuggestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View view = layoutInflater.inflate(R.layout.item_suggestion, parent, false);
        return new SuggestionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SuggestionViewHolder holder, int position) {

        holder.suggestionTextView.setText(mStringList.get(position));


    }

    @Override
    public int getItemCount() {
        return mStringList.size();
    }

    class SuggestionViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        final TextView suggestionTextView;

        public SuggestionViewHolder(View view){
            super(view);

            suggestionTextView = view.findViewById(R.id.suggestionTextView);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            mOnListItemClickListener.onListItemClick(position);
        }
    }
}
