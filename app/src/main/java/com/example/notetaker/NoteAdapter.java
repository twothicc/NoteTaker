package com.example.notetaker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {


    private Context mContext;
    private ArrayList<Message>mMessagesData;

    //Constructor of NoteAdapter will obtain the context it is used in and obtain the populated Arraylist with values that will be used for view elements
    NoteAdapter(Context context,ArrayList<Message> mMessagesData){
        this.mContext=context;
        this.mMessagesData=mMessagesData;
    }

    public void setmMessagesData(ArrayList<Message> searchResults){
        this.mMessagesData=searchResults;
    }

    //onCreateViewHolder instantiates new viewholders based on the layout defined in resources
    //an object that provides a set of LayoutParams values for root of the returned hierarchy
    //false, root is only used to create the correct subclass of LayoutParams for the root view in the XML.
    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(mContext).inflate(R.layout.note_item_layout,parent,false);
        return new NoteViewHolder(view);
    }

    //Everytime a viewholder is instantiated in onCreateViewHolder, that viewholder's view elements are set up here
    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        String Title=mMessagesData.get(position).getTitle();
        String Content=mMessagesData.get(position).getContent();
        String Date=mMessagesData.get(position).getDate();
        String Time=mMessagesData.get(position).getTime();
        int Visibility=mMessagesData.get(position).getDeleteButtonVisibility();
        String AlarmInfo=mMessagesData.get(position).getAlarmInfo();
        BindToMessage(holder,Title,Content,Date,Time,Visibility,AlarmInfo);
    }

    //getItemCount determines how many viewholders we need to create
    @Override
    public int getItemCount() {
        return mMessagesData.size();
    }

    //This is just a convenient method to set up view elements in onBindViewHolder
    private void BindToMessage(NoteViewHolder holder,String Title,String Content,String Date,String Time,int Visibility,String AlarmInfo){
        holder.mNoteTitle.setText(Title);
        holder.mNoteContent.setText(Content);
        holder.mNoteDate.setText(Date);
        holder.mNoteTime.setText(Time);
        holder.mDeleteButton.setVisibility(Visibility);
        holder.mNoteAlarm.setText(AlarmInfo);
    }

    //inner class that holds member variables that holds references to view elements that will be used
    class NoteViewHolder extends RecyclerView.ViewHolder{

        private TextView mNoteTitle;
        private TextView mNoteDate;
        private TextView mNoteTime;
        private TextView mNoteContent;
        private ImageView mDeleteButton;
        private TextView mNoteAlarm;       //////////////////////////

        NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            mNoteDate=itemView.findViewById(R.id.NoteDate);
            mNoteTime=itemView.findViewById(R.id.NoteTime);
            mNoteTitle=itemView.findViewById(R.id.NoteTitle);
            mNoteContent=itemView.findViewById(R.id.NoteContent);
            mDeleteButton=itemView.findViewById(R.id.delete_button);
            mNoteAlarm=itemView.findViewById(R.id.NoteAlarm);        ////////////////////
        }
    }

}
