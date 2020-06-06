package com.example.notetaker;

public class Message {
    private String title;
    private String content;
    private String date;
    private String time;
    private int deleteButtonVisibility;
    private int uniqueAlarmKey;
    private String AlarmInfo;
    private long AlarmTriggerTimeInMilliseconds=0;

    //Constructor for message class
    public Message(String title,String content,String date,String time,int deleteButtonVisibility,int UniqueKey,String AlarmInfo){
        this.title=title;
        this.content=content;
        this.date=date;
        this.time=time;
        this.deleteButtonVisibility=deleteButtonVisibility;
        this.uniqueAlarmKey=UniqueKey;
        this.AlarmInfo=AlarmInfo;
    }

    //Getter & Setter for title
    public String getTitle(){
        return title;
    }
    public void setTitle(String title){
        this.title=title;
    }

    //Getter & Setter for content
    public String getContent(){
        return content;
    }
    public void setContent(String content){
        this.content=content;
    }

    //Getter & Setter for date
    public String getDate(){
        return date;
    }
    public void setDate(String date){
        this.date=date;
    }

    //Getter & Setter for time
    public String getTime(){
        return time;
    }
    public void setTime(String time){
        this.time=time;
    }

    public int getDeleteButtonVisibility(){return deleteButtonVisibility;}
    public void setDeleteButtonVisibility(int Visbility){this.deleteButtonVisibility=Visbility;}

    public int getUniqueAlarmKey(){ return uniqueAlarmKey; }
    public void setUniqueAlarmKey(int UniqueKey){ this.uniqueAlarmKey=UniqueKey; }

    public String getAlarmInfo(){return AlarmInfo;}
    public void setAlarmInfo(String AlarmTime){this.AlarmInfo=AlarmTime;}

    public long getAlarmTriggerTimeInMilliseconds(){return AlarmTriggerTimeInMilliseconds;}       /////////////Used to return the AlarmTriggerTime for this message object
    public void resetAlarmTriggerTimeInMilliseconds(){this.AlarmTriggerTimeInMilliseconds=0;}     /////////////Used to set alarmtime to 0, default
    public void setAlarmTriggerTimeInMilliseconds(long AlarmTriggerTime){this.AlarmTriggerTimeInMilliseconds=AlarmTriggerTime;}    //////////////Used to set alarmtime to actual alarm trigger time
}
