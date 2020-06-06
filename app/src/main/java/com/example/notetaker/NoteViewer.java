package com.example.notetaker;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class NoteViewer extends AppCompatActivity {

    private TextView mViewNoteTitle;
    private TextView mViewNoteContent;
    private TextView mViewNoteDate;
    private TextView mViewNoteTime;
    private int mViewUniqueAlarmKeyValue;            //////////////
    private RadioButton mAlarmState;                 //////////////
    private TextView mNotificationText;

    private EditText mEditNoteTitle;
    private EditText mEditNoteContent;

    private TextWatcher mEditNoteContentTextWatcher;

    //member variables used as keys for explicit intent extras
    private static final String TITLE_TRANSFER="intent_title_transfer";
    private static final String CONTENT_TRANSFER="intent_content_transfer";
    private static final String DATE_TRANSFER="intent_date_transfer";
    private static final String TIME_TRANSFER="intent_time_transfer";
    private static final String UNIQUEALARMKEY_TRANSFER="intent_uniquealarmkey_transfer";             /////////////////////
    private static final String ALARMINFO_TRANSFER="intent_alarminfo_transfer";                   ////////////////////////////
    private static final String ALARMTRIGGERTIME_TRANSFER="intent_alarmtriggertime_transfer";      //////////////////////////////

    //member variables to hold the requestCode value and position values since we have to use them in SaveButton later to determine what results to return to MainActivity
    private int adapter_position_holder;
    private int requestCode;

    //member variables to store title and content of received intent to use for comparison later to determine if note has been edited OR /////////////if alarm is changed/added
    private String INITIAL_TITLE;
    private String INITIAL_CONTENT;
    private int INITIAL_UNIQUEKEY;

    //member variables used as keys for savedInstanceState
    private static final String TITLE_STATE="title_instance_state";
    private static final String CONTENT_STATE="content_instance_state";
    private static final String DATE_STATE="date_instance_state";
    private static final String TIME_STATE="time_instance_state";
    private static final String IS_EDITTITLE_ENABLED="is_edittitle_enabled";
    private static final String IS_EDITCONTENT_ENABLED="is_editcontent_enabled";
    private static final String REQUESTCODE_STATE="requestCode_instance_state";
    private static final String ADAPTERPOSITION_STATE="adapterposition_instance_state";
    private static final String INITIALTITLE_STATE="initialtitle_instance_state";
    private static final String INITIALCONTENT_STATE="initialcontent_instance_state";
    private static final String INITIALUNIQUEKEY_STATE="initialuniquekey_instance_state"; ///////////////////////////////////Need Key to save initial unique key through instance states
    private static final String UNIQUEALARMKEY_STATE="uniquealarmkey_instance_state";        ///////////////////////////////
    private static final String ALARMINFO_STATE="alarmInfo_instance_state";         //////////////////////////
    private static final String ALARMTRIGGERTIME_STATE="alarmtriggertime_instance_state";  /////////////////////

    //Member variables for accessing mPreference SOLELY just to set color this time
    private SharedPreferences mPreferences;
    private String sharedPrefFile="com.example.android.notesharedprefs";

    //Member variables for setting theme color correctly, since dumbass me set two separate themes to each activity just so i can have different toolbars
    private static final String THEME_COLOR_STATE="themecolor_preference";

    //Member variables for slider layout
    private BottomSheetBehavior bottomSheetBehavior;

    //Member variables for date time picker & related Alarm setting components
    private int mYEAR;
    private int mMONTH;
    private int mDAY;
    private int mHOUR;
    private int mMINUTE;

    private static final int NOTIFICATION_ID=0;

    private static final String ACTION_SET_ALARM="set_alarm_intent_action";          //////////Combine this with mViewUniqueAlarmKey to generate a unique action for set alarm
    private ArrayList<Integer> usedAlarmKeysList=new ArrayList<>();              //////////////////////////////////////
    private static final String USEDALARMKEYSLIST="used_alarmkeys_list";         //////////////////////////////////////
    private static final String USEDALARMKEYSIZE="used_alarmkeys_size";          //////////////////////////////////////
    private String alarmInfo;
    private long alarmTriggerTime;

    ////////////USED AS KEYS TO ACCESS ALARM'S PENDING INTENT'S INTENT'S DATA
    private static final String NOTIFICATION_TITLE="notification_title";
    private static final String NOTIFICATION_CONTENT="notification_content";

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

        setContentView(R.layout.activity_note_viewer);
        Toolbar toolbar=findViewById(R.id.subtoolbar);
        setSupportActionBar(toolbar);
        ActionBar subActionBar=getSupportActionBar();
        subActionBar.setDisplayHomeAsUpEnabled(true);
        subActionBar.setDisplayShowTitleEnabled(false);


        mViewNoteTitle=findViewById(R.id.View_Note_Title);
        mViewNoteContent=findViewById(R.id.View_Note_Content);
        mViewNoteDate=findViewById(R.id.View_Note_Date);
        mViewNoteTime=findViewById(R.id.View_Note_Time);

        mEditNoteTitle=findViewById(R.id.Edit_Note_Title);
        mEditNoteContent=findViewById(R.id.Edit_Note_Content);

        mAlarmState=findViewById(R.id.alarm_state);                                 //////////////////////
        mAlarmState.setClickable(false);                                       /////////////////Don't want users to be clicking on the radiobutton to change its value manually

        usedAlarmKeysList=UniqueAlarmKeyGenerator.KeysInUse;                              //////////////////////////////

        mNotificationText=findViewById(R.id.notification_text);                        ////////////////////////////


        //This TextWatcher is meant to auto-add bullets to mEditNoteContent to make point form notes
        mEditNoteContentTextWatcher=new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                int currentCursorPosition = mEditNoteContent.getSelectionEnd();
                if ((currentCursorPosition - 2) >= 0) {
                    if (s.charAt(currentCursorPosition - 2) == '\n' && s.charAt(currentCursorPosition - 1) == '.') {
                        String str = mEditNoteContent.getText().toString();
                        StringBuilder stringBuilder = new StringBuilder(str).replace(currentCursorPosition - 1, currentCursorPosition, "• ");
                        mEditNoteContent.setText(stringBuilder.toString());
                        mEditNoteContent.setSelection(currentCursorPosition + 1);
                    }
                }
            }
        };



        if(savedInstanceState==null){
            //Check for the requestcode of the explicit intent that started this activity to determine whether user wishes to add or edit notes
            Intent receivedIntent=getIntent();
            requestCode=receivedIntent.getIntExtra("request_code_value",100);
            alarmTriggerTime=receivedIntent.getLongExtra(ALARMTRIGGERTIME_TRANSFER,0);
            if(requestCode!=100){
                if(requestCode==0){
                    //If requestCode is 0, it would mean that the user intends to add a note to the recyclerview.
                    //Therefore, we'll show all the edittexts and hide all the textviews to allow user to quickly edit his note
                    mViewNoteDate.setText(getCurrentDate()); //We don't want user to temper with the date nor time.
                    mViewNoteTime.setText(getCurrentTime());

                    mAlarmState.setChecked(false);      /////////////////////////////////New Note will have no alarm set to it, so need to set alarm to false
                    alarmInfo="Set Notification Alarm";                              /////////////Need to change text for notification_text as well if alarm is not set
                    mNotificationText.setText(alarmInfo);                             ///////////////////////////

                    showHideView(mEditNoteTitle,true,0);
                    showHideView(mEditNoteContent,true,0);
                    showHideView(mViewNoteTitle,false,4);
                    showHideView(mViewNoteContent,false,4);
                }else if(requestCode==2){
                    //If requestCode is 2, it would mean that the user intends to view/edit a note
                    //Therefore, we'll have to show all textviews and hide all edittexts since user may not want to edit his note
                    //Date & Time will be extracted from intent this time
                    mViewNoteDate.setText(receivedIntent.getStringExtra(DATE_TRANSFER));
                    mViewNoteTime.setText(receivedIntent.getStringExtra(TIME_TRANSFER));
                    mViewNoteTitle.setText(receivedIntent.getStringExtra(TITLE_TRANSFER));
                    mViewNoteContent.setText(receivedIntent.getStringExtra(CONTENT_TRANSFER));
                    alarmInfo=receivedIntent.getStringExtra(ALARMINFO_TRANSFER);
                    if(!alarmInfo.equals("")){            /////////////////////////
                        mNotificationText.setText(alarmInfo);
                    }
                    mViewUniqueAlarmKeyValue=receivedIntent.getIntExtra(UNIQUEALARMKEY_TRANSFER,0);     /////////////////////////////////////////////
                    if(mViewUniqueAlarmKeyValue!=0){
                        mAlarmState.setChecked(true);
                        ////////////////////////Need to pass the text from notification_text to be stored in Message. This is also data that needs to go into mPreference///////////////FUCK
                    }else{
                        mAlarmState.setChecked(false);      /////////////////////////////////If uniquealarmkey is 0, means this note doesn't have an alarm set to it
                        alarmInfo="Set Notification Alarm";                              /////////////Need to change text for notification_text as well if alarm is not set
                        mNotificationText.setText(alarmInfo);                             ///////////////////////////
                    }
                    adapter_position_holder=receivedIntent.getIntExtra("adapter_position",0);

                    showHideView(mViewNoteTitle,true,0);
                    showHideView(mViewNoteContent,true,0);
                    showHideView(mEditNoteTitle,false,4);
                    showHideView(mEditNoteContent,false,4);

                    //We set INITIAL_TITLE & INITIAL_CONTENT to the values in the original note so we can use for comparison later to check if note has been edited
                    INITIAL_TITLE=receivedIntent.getStringExtra(TITLE_TRANSFER);
                    INITIAL_CONTENT=receivedIntent.getStringExtra(CONTENT_TRANSFER);
                    INITIAL_UNIQUEKEY=receivedIntent.getIntExtra(UNIQUEALARMKEY_TRANSFER,0);    ///////////////////////////Need to keep track if uniquekey is changed
                }
            }
        }else{ //If a saved instance state exist, we don't want to have to extract values from the intent everytime we switch to landscape
            mViewNoteDate.setText(savedInstanceState.getString(DATE_STATE));
            mViewNoteTime.setText(savedInstanceState.getString(TIME_STATE));
            INITIAL_TITLE=savedInstanceState.getString(INITIALTITLE_STATE);
            INITIAL_CONTENT=savedInstanceState.getString(INITIALCONTENT_STATE);
            INITIAL_UNIQUEKEY=savedInstanceState.getInt(INITIALUNIQUEKEY_STATE);      /////////////////////////////Maintain initial unique key thru instance states
            requestCode=savedInstanceState.getInt(REQUESTCODE_STATE);
            adapter_position_holder=savedInstanceState.getInt(ADAPTERPOSITION_STATE);
            mViewUniqueAlarmKeyValue=savedInstanceState.getInt(UNIQUEALARMKEY_STATE);              ///////////////////////////////////////////
            alarmTriggerTime=savedInstanceState.getLong(ALARMTRIGGERTIME_STATE); /////////////Need to extract this alarmtriggertime value that we saved to maintain it


            if(mViewUniqueAlarmKeyValue!=0){
                mAlarmState.setChecked(true);
                alarmInfo=savedInstanceState.getString(ALARMINFO_STATE);            //////////////////////////If instancestate exist and alarm is already set, we want to set the text to existing alarminfo
                mNotificationText.setText(alarmInfo);                         //Need to set this state as well
                ////////////////////////Need to pass the text from notification_text to be stored in Message. This is also data that needs to go into mPreference///////////////FUCK
            }else{
                mAlarmState.setChecked(false);      /////////////////////////////////If uniquealarmkey is 0, means this note doesn't have an alarm set to it
                alarmInfo="Set Notification Alarm";                              /////////////Need to change text for notification_text as well if alarm is not set
                mNotificationText.setText(alarmInfo);                             ///////////////////////////
            }


            boolean isEditTitleEnabled=savedInstanceState.getBoolean(IS_EDITTITLE_ENABLED);
            boolean isEditContentEnabled=savedInstanceState.getBoolean(IS_EDITCONTENT_ENABLED);
            if(isEditTitleEnabled){
                showHideView(mEditNoteTitle,true,0);
                showHideView(mViewNoteTitle,false,4);
                mEditNoteTitle.setText(savedInstanceState.getString(TITLE_STATE));
            }else if(!isEditTitleEnabled){
                showHideView(mViewNoteTitle,true,0);
                showHideView(mEditNoteTitle,false,4);
                mViewNoteTitle.setText(savedInstanceState.getString(TITLE_STATE));
            }
            if(isEditContentEnabled){
                showHideView(mEditNoteContent,true,0);
                showHideView(mViewNoteContent,false,4);
                mEditNoteContent.setText(savedInstanceState.getString(CONTENT_STATE));
            }else if(!isEditContentEnabled){
                showHideView(mViewNoteContent,true,0);
                showHideView(mEditNoteContent,false,4);
                mViewNoteContent.setText(savedInstanceState.getString(CONTENT_STATE));
            }

        }


        mViewNoteTitle.setOnClickListener(new View.OnClickListener() { //Simple transfer of Strings from textview to editview for editing
            @Override
            public void onClick(View v) {
                if(!mAlarmState.isChecked()){         //////////////////////IF alarm is set, we don't want users to edit note cuz alarm's notification content is already set
                    String transfer=mViewNoteTitle.getText().toString();
                    showHideView(mViewNoteTitle,false,4);
                    showHideView(mEditNoteTitle,true,0);
                    if(!transfer.equals("")){
                        mEditNoteTitle.setText(transfer);
                    }
                    mEditNoteTitle.setSelection(transfer.length()); //Use this to set the "cursor" to the end of wtvr text is in edittext.
                    mEditNoteTitle.requestFocus();
                    showKeyboard(mEditNoteTitle);
                }else{
                    String toastMessage="Alarm is already set with Note's title and content.\nPlease cancel Alarm to edit Note";
                    Toast.makeText(NoteViewer.this,toastMessage,Toast.LENGTH_SHORT).show();
                }

            }
        });

        mViewNoteContent.setOnClickListener(new View.OnClickListener() { //Simple transfer of Strings from textview to editview for editing
            @Override
            public void onClick(View v) {
                if(!mAlarmState.isChecked()){ //////////////////////IF alarm is set, we don't want users to edit note cuz alarm's notification content is already set
                    String transfer=mViewNoteContent.getText().toString();
                    showHideView(mViewNoteContent,false,4);
                    showHideView(mEditNoteContent,true,0);
                    if(!transfer.equals("")){
                        mEditNoteContent.setText(transfer);
                    }
                    mEditNoteContent.setSelection(transfer.length()); //Use this to set the "cursor" to the end of wtvr text is in edittext.
                    mEditNoteContent.requestFocus();
                    showKeyboard(mEditNoteContent);
                }else{
                    String toastMessage="Alarm is already set with Note's title and content.\nPlease cancel Alarm to edit Note";
                    Toast.makeText(NoteViewer.this,toastMessage,Toast.LENGTH_SHORT).show();
                }
            }
        });



        //Set up the bottomSliderBehavior             /////////////////////////////
        configureBackdrop();


    }

    @Override
    protected void onPause() {
        super.onPause();
        for(int i=0;i<usedAlarmKeysList.size();i++){
            mPreferences.edit().putInt(USEDALARMKEYSLIST+i,usedAlarmKeysList.get(i)).apply();       //////////////////////We need to keep track of used alarm keys
        }
        mPreferences.edit().putInt(USEDALARMKEYSIZE,usedAlarmKeysList.size()).apply();              /////////////////////We need the size to get all the numbers in usedKeys array from preferences file
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) { //Need to save every value for the note & values important to determining what result to send back
        super.onSaveInstanceState(outState);
        String savedDate=mViewNoteDate.getText().toString();
        outState.putString(DATE_STATE,savedDate);

        String savedTime=mViewNoteTime.getText().toString();
        outState.putString(TIME_STATE,savedTime);

        //Need to retain these values since they are used to determine what to send back to MainActivity in SaveButton
        outState.putInt(REQUESTCODE_STATE,requestCode);
        outState.putInt(ADAPTERPOSITION_STATE,adapter_position_holder);
        outState.putString(INITIALTITLE_STATE,INITIAL_TITLE);
        outState.putString(INITIALCONTENT_STATE,INITIAL_CONTENT);
        outState.putInt(INITIALUNIQUEKEY_STATE,INITIAL_UNIQUEKEY);                /////////////////////////////////////////Put initial unique key in outState to retain it thru config changes
        outState.putInt(UNIQUEALARMKEY_STATE,mViewUniqueAlarmKeyValue);           /////////////////////////////////////////
        outState.putString(ALARMINFO_STATE,alarmInfo);                            /////////////////Has to be saved as well (THE ALARMINFO)
        outState.putLong(ALARMTRIGGERTIME_STATE,alarmTriggerTime);          ///////////Need to maintain this across config change cuz this is impt to keep track of outdated alarms

        String savedTitle;
        String savedContent;

        if(mEditNoteTitle.isEnabled()){
            savedTitle=mEditNoteTitle.getText().toString();
            outState.putString(TITLE_STATE,savedTitle);
            outState.putBoolean(IS_EDITTITLE_ENABLED,true);
        }else if(mViewNoteTitle.isEnabled()){
            savedTitle=mViewNoteTitle.getText().toString();
            outState.putString(TITLE_STATE,savedTitle);
            outState.putBoolean(IS_EDITTITLE_ENABLED,false);
        }

        if(mEditNoteContent.isEnabled()){
            savedContent=mEditNoteContent.getText().toString();
            outState.putString(CONTENT_STATE,savedContent);
            outState.putBoolean(IS_EDITCONTENT_ENABLED,true);
        }else if(mViewNoteContent.isEnabled()){
            savedContent=mViewNoteContent.getText().toString();
            outState.putString(CONTENT_STATE,savedContent);
            outState.putBoolean(IS_EDITCONTENT_ENABLED,false);
        }

    }

    //We set options to turn on/off to enable/disable auto-add bullet list
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_noteviewer,menu);
        final MenuItem bulletSwitch=menu.findItem(R.id.bullet_switch);
        bulletSwitch.setCheckable(true);
        bulletSwitch.setIcon(R.drawable.ic_bullet_unchecked);
        bulletSwitch.setChecked(false);
        bulletSwitch.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if(item.isChecked()){
                    item.setChecked(false);
                    item.setIcon(R.drawable.ic_bullet_unchecked);
                    mEditNoteContent.removeTextChangedListener(mEditNoteContentTextWatcher);
                    Log.d("Editor", String.valueOf(item.isChecked()));
                }else if(!item.isChecked()){
                    item.setChecked(true);
                    item.setIcon(R.drawable.ic_bullet_checked);
                    //Set a textchange listener to start applying bullets on the right situations
                    mEditNoteContent.addTextChangedListener(mEditNoteContentTextWatcher);
                    Log.d("Editor", String.valueOf(item.isChecked()));
                }
                return true;
            }
        });
        MenuItem additionalOptionsButton=menu.findItem(R.id.Additional_Options_Button);
        additionalOptionsButton.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if(bottomSheetBehavior.getState()==BottomSheetBehavior.STATE_HIDDEN){
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }else if(bottomSheetBehavior.getState()==BottomSheetBehavior.STATE_EXPANDED){
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                }
                return true;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }


    //Set the bottomSheetBehavior to hidden in onCreate()     ////////////////////////////////////////////////////
    private void configureBackdrop(){

        LinearLayout SliderLayout=(LinearLayout)findViewById(R.id.Additional_Options_Slider);
        bottomSheetBehavior=BottomSheetBehavior.from(SliderLayout);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);


    }

    //Adds a function to the return to parentactivity button
    @Override
    public boolean onSupportNavigateUp() {

        if(INITIAL_UNIQUEKEY!=mViewUniqueAlarmKeyValue){
            if(requestCode==2){                                              //////////////////also set on navigate up to apply changes to alarm since alarm is set the moment alarmbutton is clicked
                Intent returnedIntent = new Intent();
                returnedIntent.putExtra(UNIQUEALARMKEY_TRANSFER, mViewUniqueAlarmKeyValue);    ////////////In MainActivity, can just check if returnedIntent contains only 2(i think i changed to detecting for TITLE_TRANSFER) extra
                returnedIntent.putExtra("adapter_position", adapter_position_holder);
                if(!alarmInfo.equals("Set Notification Alarm")){                               /////////////////Don't rly want alarm to say "Set Notification Alarm" in Note
                    returnedIntent.putExtra(ALARMINFO_TRANSFER,alarmInfo);                                ///////////////////Put in alarminfo to be transferred back, RMBR TO TAKE IT OUT
                }else{
                    returnedIntent.putExtra(ALARMINFO_TRANSFER,"");               ////////////If no alarm info, I just want to save "" aka nothing to show in Note
                }
                returnedIntent.putExtra(ALARMTRIGGERTIME_TRANSFER,alarmTriggerTime);  //////////////Need this triggertime to ensure no outdated alarms are kept

                setResult(RESULT_OK, returnedIntent);
                finish();
            }else{
                /////////////Need to cancel this alarm if it's set and user decides not to save note afterwards/////////////////////////////
                Intent alarmIntent = new Intent(this, AlarmReceiver.class);
                alarmIntent.setAction(ACTION_SET_ALARM+mViewUniqueAlarmKeyValue);
                PendingIntent pendingAlarmIntent = PendingIntent.getBroadcast(this, NOTIFICATION_ID, alarmIntent, PendingIntent.FLAG_NO_CREATE);
                AlarmManager alarmManager=(AlarmManager)getSystemService(ALARM_SERVICE);
                if (pendingAlarmIntent != null) {
                    Log.d("ALARM","Alarm successfully cancelled");
                    alarmManager.cancel(pendingAlarmIntent); // cancel alarm
                    pendingAlarmIntent.cancel(); // delete the PendingIntent
                }
                UniqueAlarmKeyGenerator.RecycleUniqueKey(mViewUniqueAlarmKeyValue);     ///////////////////////////Need to recycle the value if it was used and now u don't use it anymore
                setResult(RESULT_CANCELED);
                finish();
            }
        }else{
            if(requestCode==2){
                Intent returnIntent=new Intent();
                returnIntent.putExtra("adapter_position",adapter_position_holder);
                setResult(RESULT_CANCELED,returnIntent);
                finish();
            }else{
                setResult(RESULT_CANCELED);
                finish();
            }
        }

        return true;
    }

    //We override the dispatchTouchEvent of the activity to check whether the user has tapped anywhere outside of an edittext.
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if ( v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int)event.getRawX(), (int)event.getRawY())) {
                    Log.d("focus", "touchevent");
                    v.clearFocus();
                    if(v==mEditNoteTitle) { //Transfer the contents of edited title over to title once focus is lost on edittext
                        String transfer = ((EditText) v).getText().toString();
                        showHideView(mEditNoteTitle, false, 4);
                        if (!transfer.equals("")) {
                            mViewNoteTitle.setText(transfer);
                        } else {
                            mViewNoteTitle.setText("No Title");
                        }
                        showHideView(mViewNoteTitle, true, 0);
                    }else if(v==mEditNoteContent){ //Transfer the contents of edited content over to content once focus is lost on edittext
                        String transfer=((EditText) v).getText().toString();
                        showHideView(mEditNoteContent,false,4);
                        if(!transfer.equals("")){
                            mViewNoteContent.setText(transfer);
                        }else{
                            mViewNoteContent.setText("No Content");
                        }
                        showHideView(mViewNoteContent,true,0);
                    }
                    //This will set softkeyboard to disappear once focus is lost from the edittexts
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent(event);
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

    //Convenient method to show/hide views
    //Note: 0 for visible, 4 for invisible
    private void showHideView(View view,Boolean isEnabled,int Visibility){
        view.setEnabled(isEnabled);
        view.setVisibility(Visibility);
    }

    //Method to show soft keyboard again
    private void showKeyboard(View view){
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
    }

    public void onSaveNote(View view) {
        //We need to implement this if statement so that neither the title nor the content remains empty when we save the Note
        if(mEditNoteTitle.isEnabled()||mEditNoteContent.isEnabled()){
            if(mEditNoteTitle.isEnabled()){
                String transfer=mEditNoteTitle.getText().toString();
                showHideView(mEditNoteTitle,false,4);
                if(!transfer.equals("")){
                    mViewNoteTitle.setText(transfer);
                }else{
                    mViewNoteTitle.setText("No Title");
                }
                showHideView(mViewNoteTitle,true,0);
            }
            if(mEditNoteContent.isEnabled()){
                String transfer=mEditNoteContent.getText().toString();
                showHideView(mEditNoteContent,false,4);
                if(!transfer.equals("")){
                    mViewNoteContent.setText(transfer);
                }else{
                    mViewNoteContent.setText("No Content");
                }
                showHideView(mViewNoteContent,true,0);
            }
        }

//////////////////////////////////////////////////////////Need to track whether alarm is set, put it in returnIntent if it is and sent back to MainActivity for changes to Message/////////////////////
        //If note isn't edited, we don't need to send any data back. We will send RESULT_CANCELLED to differentiate result type from others  + //////////If alarm isn't edited
        if(requestCode==2 && INITIAL_TITLE.equals(mViewNoteTitle.getText().toString()) && INITIAL_CONTENT.equals(mViewNoteContent.getText().toString()) && INITIAL_UNIQUEKEY==mViewUniqueAlarmKeyValue) {
            Intent returnIntent = new Intent();
            returnIntent.putExtra("adapter_position", adapter_position_holder);
            setResult(RESULT_CANCELED, returnIntent);
            Log.d("Position", "No Edits");
            finish();
        }else if(requestCode==2 && INITIAL_TITLE.equals(mViewNoteTitle.getText().toString()) && INITIAL_CONTENT.equals(mViewNoteContent.getText().toString()) && INITIAL_UNIQUEKEY!=mViewUniqueAlarmKeyValue){
            Intent returnIntent=new Intent();

            //THIS IS JUST A REPLICA OF THE ONE IN ONSUPPORTNAVIGATEUP SINCE I WANT A SITUATION WHERE ALARM IS CHANGED BUT NOTE ISN'T AANNNDD I IMPLEMENTED IT THERE AND FORGOT TO IMPLEMENT HERE LMAO KMS

            returnIntent.putExtra("adapter_position",adapter_position_holder);
            returnIntent.putExtra(UNIQUEALARMKEY_TRANSFER,mViewUniqueAlarmKeyValue);       ///////////////////////////////////////Just put it in so we'll set it anyways, won't be too unoptimized
            if(!alarmInfo.equals("Set Notification Alarm")){                               /////////////////Don't rly want alarm to say "Set Notification Alarm" in Note
                returnIntent.putExtra(ALARMINFO_TRANSFER,alarmInfo);                                ///////////////////Put in alarminfo to be transferred back, RMBR TO TAKE IT OUT
            }else{
                returnIntent.putExtra(ALARMINFO_TRANSFER,"");               ////////////If no alarm info, I just want to save "" aka nothing to show in Note
            }
            returnIntent.putExtra(ALARMTRIGGERTIME_TRANSFER,alarmTriggerTime);  //////////////Need this triggertime to ensure no outdated alarms are kept

            setResult(RESULT_OK, returnIntent);
            finish();


        }else{//note/ALARM//////////////// is edited/created and we need to update all aspects of the note & pass back the position too for use in MainActivity
            mViewNoteDate.setText(getCurrentDate());
            mViewNoteTime.setText(getCurrentTime());
            Intent returnIntent=new Intent();
            returnIntent.putExtra(TITLE_TRANSFER,mViewNoteTitle.getText().toString());
            returnIntent.putExtra(CONTENT_TRANSFER,mViewNoteContent.getText().toString());
            returnIntent.putExtra(DATE_TRANSFER,mViewNoteDate.getText().toString());
            returnIntent.putExtra(TIME_TRANSFER,mViewNoteTime.getText().toString());
            returnIntent.putExtra("adapter_position",adapter_position_holder);
            returnIntent.putExtra(UNIQUEALARMKEY_TRANSFER,mViewUniqueAlarmKeyValue);       ///////////////////////////////////////Just put it in so we'll set it anyways, won't be too unoptimized
            if(!alarmInfo.equals("Set Notification Alarm")){                               /////////////////Don't rly want alarm to say "Set Notification Alarm" in Note
                returnIntent.putExtra(ALARMINFO_TRANSFER,alarmInfo);                                ///////////////////Put in alarminfo to be transferred back, RMBR TO TAKE IT OUT
            }else{
                returnIntent.putExtra(ALARMINFO_TRANSFER,"");               ////////////If no alarm info, I just want to save "" aka nothing to show in Note
            }
            returnIntent.putExtra(ALARMTRIGGERTIME_TRANSFER,alarmTriggerTime);  //////////////Need this triggertime to ensure no outdated alarms are kept

            setResult(RESULT_OK,returnIntent);
            Log.d("Position","Edits Done");
            finish();
        }
    }

    //For the RelativeLayout that acts as a button that triggers date time picker for notification alarm
    public void onSetNotificationAlarm(View view) {
        if(!mAlarmState.isChecked()){
            final View dialogView=View.inflate(this,R.layout.date_time_picker,null);
            final AlertDialog alertDialog=new AlertDialog.Builder(this).create();

            final TextView date_picked_text=dialogView.findViewById(R.id.date_picked_text);
            final TextView time_picked_text=dialogView.findViewById(R.id.time_picked_text);

            dialogView.findViewById(R.id.date_picker_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Calendar c= Calendar.getInstance();
                    int year=c.get(Calendar.YEAR);
                    int month=c.get(Calendar.MONTH);
                    int day=c.get(Calendar.DAY_OF_MONTH);
                    DatePickerDialog datePickerDialog=new DatePickerDialog(NoteViewer.this, new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                            mYEAR=year;
                            mMONTH=month;
                            mDAY=dayOfMonth;
                            String monthString=Integer.toString(month+1);
                            String yearString=Integer.toString(year);
                            String dayString=Integer.toString(dayOfMonth);
                            String toastMessage="Date set for: "+dayString+"/"+monthString+"/"+yearString;
                            date_picked_text.setText(toastMessage);
                            Toast.makeText(NoteViewer.this,toastMessage,Toast.LENGTH_SHORT).show();
                        }
                    },year,month,day);
                    datePickerDialog.show();
                }
            });
            dialogView.findViewById(R.id.time_picker_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Calendar c=Calendar.getInstance();
                    int hour=c.get(Calendar.HOUR_OF_DAY);
                    int minute=c.get(Calendar.MINUTE);
                    TimePickerDialog timePickerDialog=new TimePickerDialog(NoteViewer.this, new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                            mHOUR=hourOfDay;
                            mMINUTE=minute;
                            String hourString=Integer.toString(hourOfDay);
                            String minuteString=Integer.toString(minute);
                            String toastMessage="Time set for: "+hourString+":"+minuteString;
                            time_picked_text.setText(toastMessage);
                            Toast.makeText(NoteViewer.this,toastMessage,Toast.LENGTH_SHORT).show();
                        }
                    },hour,minute,false);
                    timePickerDialog.show();
                }
            });
            dialogView.findViewById(R.id.Alarm_confirm_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(time_picked_text.getText().toString().equals("")||date_picked_text.getText().toString().equals("")){
                        String toastMessage="Please set both a date & a time for your alarm";
                        Toast.makeText(NoteViewer.this,toastMessage,Toast.LENGTH_SHORT).show();
                    }else{
                        mViewUniqueAlarmKeyValue=UniqueAlarmKeyGenerator.GetRandomUniqueKey();     //////We must get a new value for the alarmkeyvalue to go along with intent action to make it unique
                        ////////////////Note: UniqueAlarmKey is default set to 0 to indicate no alarm is set KEEP THIS IN MIND!!/////////////////////////////////////////////////
                        final Intent alarmIntent=new Intent(getApplicationContext(),AlarmReceiver.class);
                        alarmIntent.setAction(ACTION_SET_ALARM+mViewUniqueAlarmKeyValue);

                        //I want to set these values to the notification. BUT WE MUST NOT LET USER EDIT TEXT IF ALARM IS ALREADY SET
                        alarmIntent.putExtra(NOTIFICATION_TITLE,mViewNoteTitle.getText().toString());
                        alarmIntent.putExtra(NOTIFICATION_CONTENT,mViewNoteContent.getText().toString());

                        PendingIntent pendingAlarmIntent=PendingIntent.getBroadcast(getApplicationContext(),NOTIFICATION_ID,alarmIntent,PendingIntent.FLAG_UPDATE_CURRENT);
                        AlarmManager alarmManager=(AlarmManager)getSystemService(ALARM_SERVICE);
                        if(alarmManager!=null) {
                            Calendar c = Calendar.getInstance();
                            long CurrentMilliSeconds = c.getTimeInMillis();
                            c.set(mYEAR, mMONTH, mDAY, mHOUR, mMINUTE);
                            long SetMilliSeconds = c.getTimeInMillis();
                            long triggerTimeMilliSeconds = System.currentTimeMillis()+(SetMilliSeconds - CurrentMilliSeconds);

                            alarmTriggerTime=triggerTimeMilliSeconds; ////////////////////////////////////USED TO KEEP TRACK OF ALARM TRIGGER TIME SO THAT OUTDATED ONES CAN BE REMOVED//////////////

                            Log.d("ALARM","Alarm is set to go off at"+ triggerTimeMilliSeconds);
                            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTimeMilliSeconds,pendingAlarmIntent); //////////////////////////////////////////
                        }
                        alarmInfo="Alarm set for "+mDAY+"/"+mMONTH+"/"+mYEAR+" at "+mHOUR+":"+mMINUTE;               ////////////////////////////Need to maintain this thru saved instanced state
                        mNotificationText.setText(alarmInfo);
                        mAlarmState.setChecked(true);
                        alertDialog.dismiss();
                    }
                }
            });
            alertDialog.setView(dialogView);
            alertDialog.show();
        }else{
            /////////////////////FIND A WAY TO CANCEL PENDING INTENT (THAT STACKOVERFLOW ANSWER WORKS) IMPLEMENT IT!
            Intent alarmIntent = new Intent(this, AlarmReceiver.class);
            alarmIntent.setAction(ACTION_SET_ALARM+mViewUniqueAlarmKeyValue);
            PendingIntent pendingAlarmIntent = PendingIntent.getBroadcast(this, NOTIFICATION_ID, alarmIntent, PendingIntent.FLAG_NO_CREATE);
            AlarmManager alarmManager=(AlarmManager)getSystemService(ALARM_SERVICE);
            if (pendingAlarmIntent != null && alarmManager!=null) {
                Log.d("ALARM","Alarm successfully cancelled");
                alarmManager.cancel(pendingAlarmIntent); // cancel alarm
                pendingAlarmIntent.cancel(); // delete the PendingIntent
            }
            UniqueAlarmKeyGenerator.RecycleUniqueKey(mViewUniqueAlarmKeyValue);     ///////////////////////////Need to recycle the value if it was used and now u don't use it anymore
            mViewUniqueAlarmKeyValue=0;                                        ////////////////Need to reset unique alarm key to 0 so that the return buttons can understand that no alarm is set IMPT
            mAlarmState.setChecked(false);                                       //////////////////////////////
            alarmInfo="Set Notification Alarm";                              /////////////Need to change text for notification_text as well on remove alarm
            mNotificationText.setText(alarmInfo);                             ///////////////////////////
            alarmTriggerTime=0; ////////////////////////////////////USED TO KEEP TRACK OF ALARM TRIGGER TIME SO THAT OUTDATED ONES CAN BE REMOVED//////////////
        }

    }


}
