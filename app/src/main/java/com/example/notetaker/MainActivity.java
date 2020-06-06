package com.example.notetaker;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    //Member variables used to hold data needed to display notes
    private ArrayList<String> mMessageTitles=new ArrayList<String>();
    private ArrayList<String> mMessageContents=new ArrayList<String>();
    private ArrayList<String> mMessageDates=new ArrayList<String>();
    private ArrayList<String> mMessageTimes=new ArrayList<String>();
    private ArrayList<Integer> mMessageVisibility=new ArrayList<Integer>();
    private ArrayList<Integer> mMessageUniqueAlarmKey=new ArrayList<Integer>();          /////////////////////////
    private ArrayList<String> mMessageAlarmInfo=new ArrayList<String>();         ///////////////////////
    private ArrayList<Long> mMessageAlarmTriggerTime=new ArrayList<Long>();       ///////////////////////

    private ArrayList<Integer> usedAlarmKeyList=new ArrayList<Integer>();               /////////////////////////
    private int usedAlarmKeysSize;
    private static final String USEDALARMKEYSLIST="used_alarmkeys_list";         //////////////////////////////////////
    private static final String USEDALARMKEYSIZE="used_alarmkeys_size";          //////////////////////////////////////

    private ArrayList<Message> mMessageData=new ArrayList<Message>();
    private RecyclerView recyclerView;
    private NoteAdapter mAdapter;


    //Member variables for preserving data in SharedPrefFiles
    private SharedPreferences mPreferences;
    private String sharedPrefFile="com.example.android.notesharedprefs";

    private static final String ARRAY_SIZE="array_size";
    private static int array_size;

    private static final String MESSAGE_TITLE="message_title_number";
    private static final String MESSAGE_CONTENT="message_content_number";
    private static final String MESSAGE_DATE="message_date_number";
    private static final String MESSAGE_TIME="message_time_number";
    private static final String MESSAGE_UNIQUEALARMKEY="message_uniquealarmkey_number";          //////////////
    private static final String MESSAGE_ALARMINFO="message_alarminfo_number";     ///////////////////////////////////
    private static final String MESSAGE_ALARMTRIGGERTIME="message_alarmtriggertime_number";

    //Member variables for preserving data in savedInstanceState
    private static final String DELETE_MODE_STATE="isDeleteMode_instance_state";
    private static final String DELETE_LIST_STATE="mDeleteList_instance_state";




    //Member variables for creating new notes/editing notes
    private static final int ADD_REQUEST_CODE=0;
    private static final int VIEW_REQUEST_CODE=2;
    private static final String TITLE_TRANSFER="intent_title_transfer";
    private static final String CONTENT_TRANSFER="intent_content_transfer";
    private static final String DATE_TRANSFER="intent_date_transfer";
    private static final String TIME_TRANSFER="intent_time_transfer";
    private static final String UNIQUEALARMKEY_TRANSFER="intent_uniquealarmkey_transfer";             /////////////////////
    private static final String ALARMINFO_TRANSFER="intent_alarminfo_transfer";                   ////////////////////////////
    private static final String ALARMTRIGGERTIME_TRANSFER="intent_alarmtriggertime_transfer";      //////////////////////////////

    //Member variables for deleting notes
    private boolean isDeleteMode=false;
    private ArrayList<Integer> mDeleteList=new ArrayList<>();

    private static final int NOTIFICATION_ID=0;
    private static final String ACTION_SET_ALARM="set_alarm_intent_action";          //////////Combine this with mViewUniqueAlarmKey to generate a unique action for set alarm

    //Member variables for preserving themecolor in SharedPrefFiles
    private static final String THEME_COLOR_STATE="themecolor_preference";

    //Member variables for setting up notification channel for alarm notifications
    private NotificationManager notificationManager;
    private static final String PRIMARY_CHANNEL_ID="primary_notification_channel";

    //Member variables for search slider (bottomsheetbehavior) and other search related variables
    private BottomSheetBehavior bottomSheetBehavior;
    private Message[] searchOrderedMessages;
    private boolean inSearchMode=false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Resources.Theme currentTheme=getTheme();
        mPreferences=getSharedPreferences(sharedPrefFile,MODE_PRIVATE);

        //If the retrieved string is a certain color, apply style to theme for that particular color. Also, if retrieved string gives grey, no need to apply style cuz its default color
        String user_Theme=mPreferences.getString(THEME_COLOR_STATE,"Grey Theme");
        if(!user_Theme.equals("Grey Theme")){
            switch (user_Theme){
                case "Red Theme":
                    currentTheme.applyStyle(R.style.OverlayThemeRed,true);
                    break;
                case "Blue Theme":
                    currentTheme.applyStyle(R.style.OverlayThemeBlue,true);
                    break;
                case "Green Theme":
                    currentTheme.applyStyle(R.style.OverlayThemeGreen,true);
                    break;
                case "Pink Theme":
                    currentTheme.applyStyle(R.style.OverlayThemePink,true);
                    break;
            }
        }

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        TextView toolbartext=findViewById(R.id.toolbar_text);
        setSupportActionBar(toolbar);
        //Removes the Title of the activity from the toolbar
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbartext.setText(R.string.app_name);


        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //This starts an explicit intent for result to the view/edit activity
                Intent addNoteIntent=new Intent(MainActivity.this,NoteViewer.class);
                addNoteIntent.putExtra("request_code_value",ADD_REQUEST_CODE);
                startActivityForResult(addNoteIntent,ADD_REQUEST_CODE);
            }
        });


        //Need to set the notificationManager since it'll be used to create notification channel  ///////////////////////////////////////////////////////////////////////
        notificationManager=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        usedAlarmKeysSize=mPreferences.getInt(USEDALARMKEYSIZE,0);                        /////////////////////////////Need to retrieve list
        for(int i=0;i<usedAlarmKeysSize;i++){
            usedAlarmKeyList.add(mPreferences.getInt(USEDALARMKEYSLIST+i,1));        /////////////////////////////
        }
        UniqueAlarmKeyGenerator.KeysInUse=usedAlarmKeyList;                                        /////////////////////////////We need to ensure KeysInUse is set everytime app starts into MainActivity

        //We can't save an arraylist in sharedPreferences, so we take each String out and make an array out of them.
        //We get the array_size value in the onPause method. But we still need a default value to detect when there's no messages
        array_size=mPreferences.getInt(ARRAY_SIZE,0);
        Log.d("Observer","Retrieved arraysize is"+String.valueOf(array_size));
        if(array_size!=0){
            for(int i=0;i<array_size;i++){
                String SavedTitle=mPreferences.getString(MESSAGE_TITLE+i,"Error");
                mMessageTitles.add(SavedTitle);
                String SavedContent=mPreferences.getString(MESSAGE_CONTENT+i,"Error");
                mMessageContents.add(SavedContent);
                String SavedDate=mPreferences.getString(MESSAGE_DATE+i,"Error");
                mMessageDates.add(SavedDate);
                String SavedTime=mPreferences.getString(MESSAGE_TIME+i,"Error");
                mMessageTimes.add(SavedTime);

                int SavedUniqueKeyNumber=mPreferences.getInt(MESSAGE_UNIQUEALARMKEY+i,0);   ////////////////////////
                mMessageUniqueAlarmKey.add(SavedUniqueKeyNumber);                                         ////////////////////////

                String SavedAlarmInfo=mPreferences.getString(MESSAGE_ALARMINFO+i,"");           ////////////////////Set text for AlarmInfo in recyclerView on onCreate
                mMessageAlarmInfo.add(SavedAlarmInfo);                                                      ////////////////////Need to update the ArrayList for AlarmInfo as well

                long SavedAlarmTriggerTime=mPreferences.getLong(MESSAGE_ALARMTRIGGERTIME+i,0);    //////////////////////Need this to keep track of outdated alarms
                mMessageAlarmTriggerTime.add(SavedAlarmTriggerTime);

                mMessageVisibility.add(View.INVISIBLE);
            }
        }else{
            //In the case of zero notes, a default note telling user to add a new note will be added
            mMessageTitles.add("Let's add a note!");
            mMessageContents.add("Click the + button to add a note");
            mMessageDates.add(getCurrentDate());
            mMessageTimes.add(getCurrentTime());
            mMessageUniqueAlarmKey.add(0);
            mMessageVisibility.add(View.INVISIBLE);
            mMessageAlarmInfo.add("");  //////////////////////////Need to set AlarmInfo on start as well
            mMessageAlarmTriggerTime.add((long) 0);          //////////////////////Need this to keep track of outdated alarms
        }


        mAdapter=new NoteAdapter(this,mMessageData);
        recyclerView=findViewById(R.id.NoteRecyclerView);
        recyclerView.setAdapter(mAdapter);
        recyclerView.setLayoutManager(new GridLayoutManager(this,1,RecyclerView.VERTICAL,true)); //Will have to change this spanCount value for different orientations

        //Don't want to set onclicklistener to every view created, so we use this method below
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(this, recyclerView, new mClickListener() {
            @Override
            public void onClick(int position) {

                Intent ViewNoteIntent=new Intent(MainActivity.this,NoteViewer.class);

                //We use the obtained position of clicked view in adapter to find the right Message object that has values we need

                Message message=mMessageData.get(position);
                ViewNoteIntent.putExtra("adapter_position",position);



                //We place the requestcode into the intent so that we can differentiate between the reasons for starting new activity
                //We put position into intent so that we don't have to preserve the position in Preferences(CAN GET CONFUSING MANAGING:SAVE WORK FOR LATER)
                ViewNoteIntent.putExtra("request_code_value",VIEW_REQUEST_CODE);


                ViewNoteIntent.putExtra(TITLE_TRANSFER,message.getTitle());
                ViewNoteIntent.putExtra(CONTENT_TRANSFER,message.getContent());
                ViewNoteIntent.putExtra(DATE_TRANSFER,message.getDate());
                ViewNoteIntent.putExtra(TIME_TRANSFER,message.getTime());
                ViewNoteIntent.putExtra(UNIQUEALARMKEY_TRANSFER,message.getUniqueAlarmKey());
                ViewNoteIntent.putExtra(ALARMINFO_TRANSFER,message.getAlarmInfo());        /////////////////////////////Need to put the alarminfo in intent that views notes
                ViewNoteIntent.putExtra(ALARMTRIGGERTIME_TRANSFER,message.getAlarmTriggerTimeInMilliseconds()); ////////////////////////////BROUGHT OVER ALARMTRIGGERTIME SO THAT CHANGES CAN BE MADE TO IT

                startActivityForResult(ViewNoteIntent,VIEW_REQUEST_CODE);

            }

            @Override
            public void onLongClick(View view, final int position) {
                //On longclick, we want to select the note for deletion, we'll add the selected notes adapter position to mDeleteList for possible multi-deletion
                Message message=mMessageData.get(position);
                //This prevents user from double longclicking on an already selected note as this will add two of the same position to mDeleteList for deletion=>extra unintended deletes
                if(message.getDeleteButtonVisibility()!=View.VISIBLE){
                    message.setDeleteButtonVisibility(View.VISIBLE);
                    mAdapter.notifyItemChanged(position);
                    mDeleteList.add(position);
                    isDeleteMode=true;

                }else{
                    //Added functionality: If user long clicks on a selected note, we search for its recorded position and take it off the executioner's list, then remove visibility of deletebutton
                    // if only one item is selected, even better: just wipe the executioner's list, turn isDeleteModeOff and remove visibility of deletebutton
                    if(mDeleteList.size()>1){
                        int cancelDelete=0;
                        for(int i=0;i<mDeleteList.size();i++){
                            if(mDeleteList.get(i) == position){
                                cancelDelete=i;
                            }
                        }
                        mDeleteList.remove(cancelDelete);
                        message.setDeleteButtonVisibility(View.INVISIBLE);
                        mAdapter.notifyItemChanged(position);
                    }else if(mDeleteList.size()==1){
                        mDeleteList.clear();
                        message.setDeleteButtonVisibility(View.INVISIBLE);
                        mAdapter.notifyItemChanged(position);
                        isDeleteMode=false;
                    }
                }
                Log.d("POSITION","Adapter position is"+position);
            }

            @Override
            public void onExtraSelection(View view, int position) {
                /** to make this workable, i've fallen to the dark side of unoptimized code by integrating an extra View.Visibility value to every single viewholder in the recyclerview**/
                //On extra selection is a simple click that follows onLongClick
                ImageView deleteButton=view.findViewById(R.id.delete_button);
                if(deleteButton.getVisibility()==View.INVISIBLE){
                    Message message=mMessageData.get(position);
                    message.setDeleteButtonVisibility(View.VISIBLE);
                    mAdapter.notifyItemChanged(position);
                    mDeleteList.add(position);
                }else if(deleteButton.getVisibility()!=View.INVISIBLE){
                    AlertDialog deleteAlert=new AlertDialog.Builder(MainActivity.this).setTitle("Delete Alert")
                            .setMessage("Are you sure you want to delete selected messages<?>")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Collections.sort(mDeleteList,Collections.<Integer>reverseOrder());
                                    for(int i=0;i<mDeleteList.size();i++){
                                        int deleteposition= mDeleteList.get(i);

                                        Message message=mMessageData.get(deleteposition);
                                        if(!message.getAlarmInfo().equals("")){
                                            //Since message object is going to be deleted anyways, no need to delete alarminfo or alarmtriggertime, just need to recycle Key & delete alarm
                                            int uniqueAlarmKey=message.getUniqueAlarmKey();
                                            String intentAction=ACTION_SET_ALARM+uniqueAlarmKey;
                                            Intent alarmIntent = new Intent(getApplicationContext(), AlarmReceiver.class);             /////Delete alarm using same intentAction
                                            alarmIntent.setAction(intentAction);
                                            PendingIntent pendingAlarmIntent = PendingIntent.getBroadcast(getApplicationContext(),NOTIFICATION_ID , alarmIntent, PendingIntent.FLAG_NO_CREATE);
                                            AlarmManager alarmManager=(AlarmManager)getSystemService(ALARM_SERVICE);
                                            if (pendingAlarmIntent != null && alarmManager!=null) {
                                                Log.d("ALARM","Alarm successfully cancelled");
                                                alarmManager.cancel(pendingAlarmIntent); // cancel alarm
                                                pendingAlarmIntent.cancel(); // delete the PendingIntent
                                            }
                                            UniqueAlarmKeyGenerator.RecycleUniqueKey(uniqueAlarmKey);          /////Recycled Key here

                                        }

                                        mMessageData.remove(deleteposition);
                                        mAdapter.notifyItemRemoved(deleteposition);
                                    }
                                    mDeleteList.clear();
                                    isDeleteMode=false;
                                }
                            }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    for(Integer i:mDeleteList){
                                        int revertposition=i;
                                        Message message=mMessageData.get(revertposition);
                                        message.setDeleteButtonVisibility(View.INVISIBLE);
                                        mAdapter.notifyItemChanged(revertposition);
                                    }
                                    mDeleteList.clear();
                                    isDeleteMode=false;
                                }
                            }).setOnCancelListener(new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialog) {
                                    for(Integer i:mDeleteList){
                                        int revertposition=i;
                                        Message message=mMessageData.get(revertposition);
                                        message.setDeleteButtonVisibility(View.INVISIBLE);
                                        mAdapter.notifyItemChanged(revertposition);
                                    }
                                    mDeleteList.clear();
                                    isDeleteMode=false;
                                }
                            }).show();
                }
            }
        }));

        //Populating the mMessageData which is the arraylist with objects that have data we use to set data for view elements in each ViewHolder
        onInitialize();

        if(savedInstanceState==null){
            isDeleteMode=false;
            mDeleteList.clear();

            recyclerView.smoothScrollToPosition(mMessageData.size());

        }else{
            isDeleteMode=savedInstanceState.getBoolean(DELETE_MODE_STATE);
            if(isDeleteMode){
                mDeleteList=savedInstanceState.getIntegerArrayList(DELETE_LIST_STATE);
                if(mDeleteList!=null){
                    //Set the items that user's have already selected for deletion if config change occurs while user is still deleting notes
                    for(Integer i:mDeleteList){
                        Message message=mMessageData.get(i);
                        message.setDeleteButtonVisibility(View.VISIBLE);
                        mAdapter.notifyItemChanged(i);
                    }
                }
            }else{
                mDeleteList.clear();
            }
        }

        //Set navigation view listener
        NavigationView navigationView=findViewById(R.id.ColorThemeNavigationView);
        /** SHIT NO WORK WTF FUCK THIS SHIT
        Menu NavMenu=navigationView.getMenu();
        switch (user_Theme){
            case "Red Theme":
                NavMenu.findItem(R.id.RedThemeSelection).getActionView().performClick();
                break;
            case "Blue Theme":
                NavMenu.findItem(R.id.BlueThemeSelection).getActionView().performClick();
                break;
            case "Green Theme":
                NavMenu.findItem(R.id.GreenThemeSelection).getActionView().performClick();
                break;
            case "Pink Theme":
                NavMenu.findItem(R.id.PinkThemeSelection).getActionView().performClick();
                break;
            case "Grey Theme":
                NavMenu.findItem(R.id.GreyThemeSelection).getActionView().performClick();
                break;
        }**/
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch(item.getItemId()) {
                    case R.id.RedThemeSelection:
                        //I save the string to match the theme color and recreate activity so that theme style can be applied in oncreate()
                        mPreferences.edit().putString(THEME_COLOR_STATE,"Red Theme").apply();
                        recreate();
                        break;
                    case R.id.GreenThemeSelection:
                        mPreferences.edit().putString(THEME_COLOR_STATE,"Green Theme").apply();
                        recreate();
                        break;
                    case R.id.BlueThemeSelection:
                        mPreferences.edit().putString(THEME_COLOR_STATE,"Blue Theme").apply();
                        recreate();
                        break;
                    case R.id.PinkThemeSelection:
                        mPreferences.edit().putString(THEME_COLOR_STATE,"Pink Theme").apply();
                        recreate();
                        break;
                    case R.id.GreyThemeSelection:
                        mPreferences.edit().putString(THEME_COLOR_STATE,"Grey Theme").apply();
                        recreate();
                        break;
                }
                DrawerLayout drawerLayout=findViewById(R.id.ColorThemeDrawer);
                drawerLayout.closeDrawer(Gravity.LEFT);
                return true;
            }
        });

        //Set up NotificationChannel
        createNotificationChannel(); //////////////////////////////////////////////////

        //configureBackDrop();////////////////////////////////CONFIGURE BOTTOMSHEETBEHAVIOR

        //configureSearchView();/////////////////////CONFIGURE SEARCHVIEW TO DISPLAY NEW ARRAYLIST DATA ON SEARCH
        inSearchMode=false; ////////Just set it back to false for clarity

    }


    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        //Have to let user stay in deletemode if config change occurs while user is selecting notes to delete + Selected notes' adapter positions have to be saved
        super.onSaveInstanceState(outState);
        outState.putBoolean(DELETE_MODE_STATE,isDeleteMode);
        outState.putIntegerArrayList(DELETE_LIST_STATE,mDeleteList);
    }




    @Override
    protected void onPause() {
        super.onPause();
        array_size=mMessageData.size(); //We obtain the array size right b4 activity is ended so we can store the right number of values into the arraylists.
        SharedPreferences.Editor mPreferencesEditor=mPreferences.edit();
        Log.d("Observer",String.valueOf(array_size));
        mPreferencesEditor.putInt(ARRAY_SIZE,array_size);
        for(int i=0;i<array_size;i++){
            mPreferencesEditor.putString(MESSAGE_TITLE+i,mMessageData.get(i).getTitle());
            mPreferencesEditor.putString(MESSAGE_CONTENT+i,mMessageData.get(i).getContent());
            mPreferencesEditor.putString(MESSAGE_DATE+i,mMessageData.get(i).getDate());
            mPreferencesEditor.putString(MESSAGE_TIME+i,mMessageData.get(i).getTime());
            mPreferencesEditor.putInt(MESSAGE_UNIQUEALARMKEY+i,mMessageData.get(i).getUniqueAlarmKey());    /////////////////////Need to save unique alarm key with Message object
            mPreferencesEditor.putString(MESSAGE_ALARMINFO+i,mMessageData.get(i).getAlarmInfo());
            mPreferencesEditor.putLong(MESSAGE_ALARMTRIGGERTIME+i,mMessageData.get(i).getAlarmTriggerTimeInMilliseconds()); ///////////USED TO FIND OUTDATED ALARMS AND REMOVE THEM
        }
        mPreferencesEditor.apply();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==ADD_REQUEST_CODE&&resultCode==RESULT_OK) {  //If note was created, insert note at top
            Log.d("Observer","NOTE ADDED, COMMENCE CHANGES");
            String returnedTitle = data.getStringExtra(TITLE_TRANSFER);
            String returnedContent = data.getStringExtra(CONTENT_TRANSFER);
            String returnedDate = data.getStringExtra(DATE_TRANSFER);
            String returnedTime = data.getStringExtra(TIME_TRANSFER);
            int returnedUniqueKey=data.getIntExtra(UNIQUEALARMKEY_TRANSFER,0);          //////////////////////////
            String returnedAlarmInfo=data.getStringExtra(ALARMINFO_TRANSFER);                           //////////////////////Get alarm info
            long returnedAlarmTriggerTime=data.getLongExtra(ALARMTRIGGERTIME_TRANSFER,0);    ///////////USED TO FIND OUTDATED ALARMS AND REMOVE THEM

            Message message=new Message(returnedTitle, returnedContent, returnedDate, returnedTime,View.INVISIBLE,returnedUniqueKey,returnedAlarmInfo);     //////////////
            long currentSystemTime=System.currentTimeMillis();
            if(returnedAlarmTriggerTime<=currentSystemTime){  ////////////This is after onCreate() so we can't check for outdated alarms. If user sets an outdated alarm & returns, we must immediately reset alarm trigger time
                message.setAlarmInfo("");                               ////////////     We want to remove the alarmInfo as well if its outdated
                UniqueAlarmKeyGenerator.RecycleUniqueKey(message.getUniqueAlarmKey());       //////////We also want to recycle the key used by this outdated alarm
                message.setUniqueAlarmKey(0);                         ///////////After we recycle the key, we'll set the UniqueAlarmKey to 0 since we're planning to delete the alarm anyways
                message.resetAlarmTriggerTimeInMilliseconds();        //////////Reset the alarmtriggertime: this basically just sets it back to 0
            }else{
                message.setAlarmTriggerTimeInMilliseconds(returnedAlarmTriggerTime);                                                                            //////////////
            }
            mMessageData.add(message);      //////////////////////////Need to pass unique key & alarmInfo
            mAdapter.notifyItemInserted(mMessageData.size());
            recyclerView.smoothScrollToPosition(mMessageData.size());     //////////I want recyclerView to scroll to the newly added note
        }
        if(requestCode==VIEW_REQUEST_CODE && resultCode==RESULT_OK) { //If note was edited, remove the original note and create a new one with the new values: insert at top
            Bundle bundle=data.getExtras();                      //////////////////////Get all the extras to check them for conditions
            if(bundle.containsKey(TITLE_TRANSFER)){                //////////////see if it contains said Key
                Log.d("Observer","NOTE EDITTED, COMMENCE CHANGES");
                String returnedTitle = data.getStringExtra(TITLE_TRANSFER);
                String returnedContent = data.getStringExtra(CONTENT_TRANSFER);
                String returnedDate = data.getStringExtra(DATE_TRANSFER);
                String returnedTime = data.getStringExtra(TIME_TRANSFER);
                int returnedUniqueKey=data.getIntExtra(UNIQUEALARMKEY_TRANSFER,0); /////////////////////////////
                int returnedAdapterPosition = data.getIntExtra("adapter_position", 0);
                String returnedAlarmInfo=data.getStringExtra(ALARMINFO_TRANSFER);       //////////////Get alarm info
                long returnedAlarmTriggerTime=data.getLongExtra(ALARMTRIGGERTIME_TRANSFER,0);   ///////////USED TO FIND OUTDATED ALARMS AND REMOVE THEM

                mMessageData.remove(returnedAdapterPosition);
                mAdapter.notifyItemRemoved(returnedAdapterPosition);
                Message message=new Message(returnedTitle, returnedContent, returnedDate, returnedTime,View.INVISIBLE,returnedUniqueKey,returnedAlarmInfo); //////////////
                long currentSystemTime=System.currentTimeMillis();
                if(returnedAlarmTriggerTime<=currentSystemTime){  ////////////This is after onCreate() so we can't check for outdated alarms. If user sets an outdated alarm & returns, we must immediately reset alarm trigger time
                    message.setAlarmInfo("");                               ////////////     We want to remove the alarmInfo as well if its outdated
                    UniqueAlarmKeyGenerator.RecycleUniqueKey(message.getUniqueAlarmKey());       //////////We also want to recycle the key used by this outdated alarm
                    message.setUniqueAlarmKey(0);                         ///////////After we recycle the key, we'll set the UniqueAlarmKey to 0 since we're planning to delete the alarm anyways
                    message.resetAlarmTriggerTimeInMilliseconds();        //////////Reset the alarmtriggertime: this basically just sets it back to 0
                }else{
                    message.setAlarmTriggerTimeInMilliseconds(returnedAlarmTriggerTime);                                                                            //////////////
                }
                mMessageData.add(message);                                          /////////////////////
                mAdapter.notifyItemInserted(mMessageData.size());
                recyclerView.smoothScrollToPosition(mMessageData.size());     //////////I want recyclerView to scroll to the newly added note
            }else{
                int returnedAdapterPosition=data.getIntExtra("adapter_position", 0);  /////////////////
                int returnedUniqueKey=data.getIntExtra(UNIQUEALARMKEY_TRANSFER,0); /////////////////////////////
                String returnedAlarmInfo=data.getStringExtra(ALARMINFO_TRANSFER); //////////////Lmao forgot to extract the alarm info
                long returnedAlarmTriggerTime=data.getLongExtra(ALARMTRIGGERTIME_TRANSFER,0);        ///////////USED TO FIND OUTDATED ALARMS AND REMOVE THEM
                Message message=mMessageData.get(returnedAdapterPosition);                    ////////////change the specific values in said message only
                message.setUniqueAlarmKey(returnedUniqueKey);                                 //////////////
                message.setAlarmInfo(returnedAlarmInfo);
                long currentSystemTime=System.currentTimeMillis();
                if(returnedAlarmTriggerTime<=currentSystemTime){  ////////////This is after onCreate() so we can't check for outdated alarms. If user sets an outdated alarm & returns, we must immediately reset alarm trigger time
                    message.setAlarmInfo("");                               ////////////     We want to remove the alarmInfo as well if its outdated
                    UniqueAlarmKeyGenerator.RecycleUniqueKey(message.getUniqueAlarmKey());       //////////We also want to recycle the key used by this outdated alarm
                    message.setUniqueAlarmKey(0);                         ///////////After we recycle the key, we'll set the UniqueAlarmKey to 0 since we're planning to delete the alarm anyways
                    message.resetAlarmTriggerTimeInMilliseconds();        //////////Reset the alarmtriggertime: this basically just sets it back to 0
                }else{
                    message.setAlarmTriggerTimeInMilliseconds(returnedAlarmTriggerTime);                                                                            //////////////
                }
                mAdapter.notifyItemChanged(returnedAdapterPosition);                          /////////////////////////////
                recyclerView.smoothScrollToPosition(returnedAdapterPosition);     //////////I want recyclerView to scroll to the newly edited note
            }
        }
        if(requestCode==VIEW_REQUEST_CODE && resultCode==RESULT_CANCELED){
            int returnedAdapterPosition=data.getIntExtra("adapter_position",0);       ////////////////
            Message message=mMessageData.get(returnedAdapterPosition);
            message.setAlarmInfo("");
            message.setUniqueAlarmKey(0);
            mAdapter.notifyItemChanged(returnedAdapterPosition);

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id){
            case R.id.color_theme_settings:
                DrawerLayout drawerLayout=findViewById(R.id.ColorThemeDrawer);
                drawerLayout.openDrawer(Gravity.LEFT);
                break;
            /**case R.id.search_note_button:
                /////////////NEED TO CONFIGURE BOTTOMSHEETBEHAVIOR
                if(bottomSheetBehavior.getState()==BottomSheetBehavior.STATE_HIDDEN){
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }else if(bottomSheetBehavior.getState()==BottomSheetBehavior.STATE_EXPANDED){
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                    mAdapter.setmMessagesData(mMessageData);                         ////////////////// need to give adapter the original data back once search is over
                    recyclerView.smoothScrollToPosition(mMessageData.size());   ////////////
                    mAdapter.notifyDataSetChanged();
                    inSearchMode=false;
                }**/
        }


        return super.onOptionsItemSelected(item);
    }

    private void configureBackDrop(){        /////////////////////////////SET BOTTOMSLIDERBEHAVIOR TO HIDDEN FIRST
        LinearLayout SliderLayout=findViewById(R.id.search_Slider);
        bottomSheetBehavior=BottomSheetBehavior.from(SliderLayout);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    /**HashMap<String,Integer> adapterPositions=new HashMap<String, Integer>();
    SearchView searchView;
    ArrayList<Message> searchResults;

    private void configureSearchView(){        ////////////Set up searchview
        SearchManager searchManager=(SearchManager)getSystemService(SEARCH_SERVICE);
        searchView=findViewById(R.id.searchview);
        searchView.setSubmitButtonEnabled(true);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                inSearchMode=true;         ///////////Set it to inSearchMode=true, so that we can send the right adapterposition on click to potentially remove the right Note in mMessageData on note edit
                int size=mMessageData.size();
                searchOrderedMessages=new Message[size];
                for(int i=0;i<size;i++){
                    searchOrderedMessages[i]=mMessageData.get(i);
                    adapterPositions.put(mMessageData.get(i).getTitle(),i);
                }
                sort(searchOrderedMessages,0,searchOrderedMessages.length-1,query);
                searchResults=new ArrayList<Message>(Arrays.asList(searchOrderedMessages));
                mAdapter.setmMessagesData(searchResults);
                mAdapter.notifyDataSetChanged();
                recyclerView.smoothScrollToPosition(size);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    //Use MergeSort to sort the similarity values returned by editDistance performed on the title and the searched string
    private void merge(Message[] arr,String searchedText,int l,int m,int r) {
        int n1 = m-l + 1;
        int n2 = r-m;
        Message[] L = new Message[n1];
        Message[] R = new Message[n2];
        for (int i = 0; i < n1; ++i) {
            L[i] = arr[l+i];
        }
        for (int j = 0; j < n2; ++j) {
            R[j] = arr[m+1+j];
        }

        int i = 0;
        int j = 0;

        int k=l;
        while (i < n1 && j < n2) {
            if (minDistance(L[i].getTitle(), searchedText) > minDistance(R[j].getTitle(), searchedText)) {
                arr[k] = L[i];
                i++;
            } else {
                arr[k] = R[j];
                j++;
            }
            k++;
        }
        while (i < n1) {
            arr[k] = L[i];
            i++;
            k++;
        }
        while (j < n2) {
            arr[k] = R[j];
            j++;
            k++;
        }
    }
    private void sort(Message[] arr,int l, int r,String searchedText){
        if(l<r){
            int m=(l+r)/2;
            sort(arr,l,m,searchedText);
            sort(arr,m+1,r,searchedText);
            merge(arr,searchedText,l,m,r);
        }
    }

    public static int minDistance(String word1, String word2) {

        String word1s=word1.toLowerCase();
        String word2s=word2.toLowerCase();
        int len1 = word1.length();
        int len2 = word2.length();

        //We don't want the output to be affected by extra character deletes. We are searching by similarity of text, not by length of text
        int extraDeletes;
        if(len1>len2){
            extraDeletes=len1-len2;
        }else{
            extraDeletes=len2-len1;
        }

        // len1+1, len2+1, because finally return dp[len1][len2]
        int[][] dp = new int[len1 + 1][len2 + 1];
        for (int i = 0; i <= len1; i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= len2; j++) {
            dp[0][j] = j;
        }
        //iterate though, and check last char
        for (int i = 0; i < len1; i++) {
            char c1 = word1s.charAt(i);
            for (int j = 0; j < len2; j++) {
                char c2 = word2s.charAt(j);
                //if last two chars equal
                if (c1 == c2) {
                    //update dp value for +1 length
                    dp[i + 1][j + 1] = dp[i][j];
                } else {
                    int replace = dp[i][j] + 1;
                    int insert = dp[i][j + 1] + 1;
                    int delete = dp[i + 1][j] + 1;

                    int min = Math.min(replace, insert);
                    min = Math.min(delete, min);
                    dp[i + 1][j + 1] = min;
                }
            }
        }

        //SUBTRACTED THE EXTRA DELETES CAUSED BY EXTRA CHARACTERS FROM THE TOTAL OUTPUT
        return dp[len1][len2]-extraDeletes;
    }**/


    //Call this method in onCreate to populate the mMessageData with all of the user's notes
    private void onInitialize(){
        mMessageData.clear();
        long currentSystemTime=System.currentTimeMillis();
        for(int i=0;i<mMessageTitles.size();i++){
            Message mMessage=new Message(mMessageTitles.get(i),mMessageContents.get(i),mMessageDates.get(i),mMessageTimes.get(i),mMessageVisibility.get(i),mMessageUniqueAlarmKey.get(i),mMessageAlarmInfo.get(i));   ////////////
            long alarmTriggerTime=mMessageAlarmTriggerTime.get(i);
            if(alarmTriggerTime>=currentSystemTime){
                mMessage.setAlarmTriggerTimeInMilliseconds(mMessageAlarmTriggerTime.get(i));              /////////////////////Set the triggertime to remove outdated alarms
            }else{
                mMessage.setAlarmInfo("");                               ////////////     We want to remove the alarmInfo as well if its outdated
                UniqueAlarmKeyGenerator.RecycleUniqueKey(mMessage.getUniqueAlarmKey());       //////////We also want to recycle the key used by this outdated alarm
                mMessage.setUniqueAlarmKey(0);                         ///////////After we recycle the key, we'll set the UniqueAlarmKey to 0 since we're planning to delete the alarm anyways
                mMessage.resetAlarmTriggerTimeInMilliseconds();        //////////Reset the alarmtriggertime: this basically just sets it back to 0
            }

            mMessageData.add(mMessage);
        }
        /*mMessageTitles.clear();
        mMessageContents.clear();
        mMessageDates.clear();
        mMessageTimes.clear();*/ //We'll test whether this is okay later. Lets assume we did this

        //Need to notify NoteAdapter of changes so that it'll be implemented
        mAdapter.notifyDataSetChanged();
        Log.d("Observer","recyclerView data loaded");
    }

    //Method to obtain date in EEE, dd/mm/yyyy format
    private String getCurrentDate(){
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("EEE, dd/MM/yyyy");
        return formatter.format(date);
    }

    //Method to obtain time in HH/mm/ss format
    private String getCurrentTime(){
        Date time=new Date();
        SimpleDateFormat formatter=new SimpleDateFormat("HH:mm");
        return formatter.format(time);
    }


    //As long as this is called once, its fine cuz changes are made to system's notificationManager and saved there
    private void createNotificationChannel(){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            NotificationChannel notificationChannel=new NotificationChannel(PRIMARY_CHANNEL_ID,"NoteTaker Notification Alarm", NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.YELLOW);
            notificationChannel.enableVibration(true);
            notificationChannel.setDescription("Alarm from NoteTaker");
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }





    //Interface for onItemTouchListener that allows us to create methods with generated parameter position later on in onCreate()
    public interface mClickListener{
        void onClick(int position);
        void onLongClick(View view,int position);
        void onExtraSelection(View view,int position);
    }

    //Inner class for onitemtouchlistener
    public class RecyclerTouchListener implements RecyclerView.OnItemTouchListener{

        private GestureDetector gestureDetector;
        private mClickListener mClickListener;

        public RecyclerTouchListener(Context context,final RecyclerView recyclerView,final mClickListener mClickListener){
            this.mClickListener=mClickListener;
            gestureDetector=new GestureDetector(context,new GestureDetector.SimpleOnGestureListener(){
                //using boolean isLongClick to differentiate between longclick and simple click
                boolean isLongClick=false;

                @Override
                public boolean onDown(MotionEvent e) {
                    isLongClick=false;
                    return false;
                }

                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    //Event will be consumed by gesture detector allowing onTouchEvent to return true below
                    if(isLongClick){
                        return false;
                    }else{
                        return true;
                    }
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    super.onLongPress(e);
                    isLongClick=true;
                    View child=recyclerView.findChildViewUnder(e.getX(),e.getY());
                    if(child!=null&&mClickListener!=null){
                        mClickListener.onLongClick(child,recyclerView.getChildAdapterPosition(child));
                    }

                }
            });
        }


        @Override
        public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
            View child=rv.findChildViewUnder(e.getX(),e.getY());
            //onTouchEvent returns true if gesturedetector does consume an event
            if(child!=null&&mClickListener!=null&&gestureDetector.onTouchEvent(e)&&!isDeleteMode){
                //this will override this class's mClickListener interface's onClick method to have a generated parameter position(adapter position of child)
                mClickListener.onClick(rv.getChildAdapterPosition(child));
            }else if(child!=null&&mClickListener!=null&&gestureDetector.onTouchEvent(e)&&isDeleteMode){
                /**after longclick activates isDeleteMode, any selection afterwards shld make delete note button visible & clickable**/
                mClickListener.onExtraSelection(child,rv.getChildAdapterPosition(child));
            }
            //Returns false to allow touchEvent to be passed to activity for processing
            return false;
        }

        @Override
        public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        }
    }
}
