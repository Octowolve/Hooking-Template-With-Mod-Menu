package com.dark.force;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.text.Html;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class MenuService extends Service {


    public View mFloatingView;
    private LinearLayout modBody;
    private WindowManager windowManager;

    public IBinder onBind(Intent intent) {
        return null;
    }

    //Override our Start Command so the Service doesnt try to recreate itself when the App is closed
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_NOT_STICKY;
    }

    //Same as above so it wont crash in the background and therefore use alot of Battery life
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        stopSelf();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        super.onTaskRemoved(rootIntent);
    }

    //Destroy our View
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mFloatingView != null)
            windowManager.removeView(mFloatingView);
        Toast.makeText(getBaseContext(), "MenuService Stopped", Toast.LENGTH_SHORT).show();
    }


    //When this Class is called the code in this function will be executed
    public void onCreate() {
        super.onCreate();
        //A little message for the user when he opens the app
        Toast.makeText(this, "Happy Modding!", Toast.LENGTH_LONG).show();
        //Init Lib
        NativeLibrary.init();
        //Create our Menu
        CreateMenu();
        //Create a handler for this Class
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            public void run() {
                MenuService.this.Thread();
                handler.postDelayed(this, 1000);
            }
        });
    }

    //Here we write the code for our Menu
    private void CreateMenu(){

        //Variable to check later if the phone supports Draw over other apps permission
        int i = Build.VERSION.SDK_INT >= 26 ? 2038 : 2002;

        //Creating our layout here
        FrameLayout frameLayout = new FrameLayout(this);
        frameLayout.setLayoutParams(new FrameLayout.LayoutParams(-2, -2));

        //The Button layout so we can open and close the Mod Menu
        RelativeLayout relativeLayout = new RelativeLayout(this); //Floatin Button
        relativeLayout.setLayoutParams(new RelativeLayout.LayoutParams(-1, -1));

        //Our ImageView handler which will be used for our Open/Closed button
        ImageView imageView = new ImageView(this);
        imageView.setLayoutParams(new RelativeLayout.LayoutParams(convertDipToPixels(50.0f), convertDipToPixels(50.0f)));

        try {
            //Load Image (wolve.png is the Image I use. If your button image has another name change it here)
            InputStream open = getAssets().open("wolve.png");
            imageView.setImageDrawable(Drawable.createFromStream(open, null));
            open.close();
            relativeLayout.addView(imageView);
            this.mFloatingView = relativeLayout;

            //Main UI (Our background Image for the menu. Again change the name if your background name IMage has another name)
            InputStream open2 = getAssets().open("ghwallpaper.jpg");
            LinearLayout linearLayout = new LinearLayout(this);
            linearLayout.setLayoutParams(new LinearLayout.LayoutParams(-1, -1));
            linearLayout.setBackground(Drawable.createFromStream(open2, null));
            linearLayout.setOrientation(LinearLayout.VERTICAL);

            //Head Text (Creates a Header text. Credit yourself, and me ples)
            TextView textView = new TextView(this);
            textView.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
            textView.setGravity(1);
            textView.setText(Html.fromHtml("<b>Mod by Octowolve</b>"));
            textView.setTextColor(Color.parseColor("#d83868"));
            textView.setTextSize(15.0f);

            //Scrollview for Toggles and Main Body
            ScrollView scrollView = new ScrollView(this);
            scrollView.setLayoutParams(new LinearLayout.LayoutParams(-1, convertDipToPixels(250.0f)));
            scrollView.setScrollBarSize(convertDipToPixels(5.0f));
            this.modBody = new LinearLayout(this);
            this.modBody.setLayoutParams(new LinearLayout.LayoutParams(-1, -1));
            this.modBody.setOrientation(LinearLayout.VERTICAL);

            addSpacing("Toggles");
            
            //Change Toggle (Due to a JNI function we can handle if a toggle has been changed)
            String[] listFT = NativeLibrary.getListFT();
            for (int i2 = 0; i2 < listFT.length; i2++) {
                final int l2 = i2;
                addToggle(listFT[i2], new CompoundButton.OnCheckedChangeListener() {
                    public void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                        NativeLibrary.changeToggle(l2);
                    }
                });
            }
            
            addSpacing("Buttons");

            addButton("Test", new CompoundButton.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(true){
                        Toast.makeText(NewMenuService.this, "Dropped AK47", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(NewMenuService.this, "Couldn't throw weapon", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            addSpacing("Seekbars");

            addSeekBar("Test", 100, new SeekBar.OnSeekBarChangeListener() {
                public void onProgressChanged(SeekBar seekBar, int i, boolean z) {
                }

                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                public void onStopTrackingTouch(SeekBar seekBar) {
                    int progress = seekBar.getProgress();
                }
            });


            //Add Body to ScrollView
            scrollView.addView(this.modBody);

            //Add Everything to LinearLayout
            linearLayout.addView(textView);
            linearLayout.addView(scrollView);
            frameLayout.addView(linearLayout);

            //Create Floating View
            final AlertDialog create = new AlertDialog.Builder(this, 2).create();
            Objects.requireNonNull(create.getWindow()).setType(i);
            create.setView(frameLayout);
            final WindowManager.LayoutParams layoutParams3 = new WindowManager.LayoutParams(-2, -2, i, 8, -3);
            layoutParams3.gravity = 51;
            layoutParams3.x = 0;
            layoutParams3.y = 100;
            this.windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
            this.windowManager.addView(this.mFloatingView, layoutParams3);
            this.mFloatingView.setOnTouchListener(new View.OnTouchListener() {
                private float initialTouchX;
                private float initialTouchY;
                private int initialX;
                private int initialY;

                @SuppressLint("ClickableViewAccessibility")
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    switch (motionEvent.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            this.initialX = layoutParams3.x;
                            this.initialY = layoutParams3.y;
                            this.initialTouchX = motionEvent.getRawX();
                            this.initialTouchY = motionEvent.getRawY();
                            return true;
                        case MotionEvent.ACTION_UP:
                            int Xdiff = (int) (motionEvent.getRawX() - initialTouchX);
                            int Ydiff = (int) (motionEvent.getRawY() - initialTouchY);

                            //The check for Xdiff <10 && YDiff< 10 because sometime elements moves a little while clicking.
                            //So that is click event.
                            if (Xdiff < 10 && Ydiff < 10) {
                                create.show();
                            }
                            return true;
                        case MotionEvent.ACTION_MOVE:
                            float round = (float) Math.round(motionEvent.getRawX() - this.initialTouchX);
                            float round2 = (float) Math.round(motionEvent.getRawY() - this.initialTouchY);
                            layoutParams3.x = this.initialX + ((int) round);
                            layoutParams3.y = this.initialY + ((int) round2);
                            MenuService.this.windowManager.updateViewLayout(MenuService.this.mFloatingView, layoutParams3);
                            return true;
                        default:
                            return false;
                    }
                }
            });

        }
        catch (IOException ignored){

        }

    }


    //Just a little function to draw a toggle so we dont have to do this all the time when we want to draw one
    private void addToggle(String str, CompoundButton.OnCheckedChangeListener onCheckedChangeListener) {
        Switch switchR = new Switch(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -1);
        layoutParams.setMargins(0, 2, 0, 0);
        switchR.setLayoutParams(layoutParams);
        //switchR.setPadding(10, 5, 10, 5);
        switchR.setText(str);
        switchR.setTextColor(Color.parseColor("#000000"));
        switchR.setTextSize(10.0f);
        switchR.setTypeface(switchR.getTypeface(), Typeface.BOLD);
        switchR.setOnCheckedChangeListener(onCheckedChangeListener);
        this.modBody.addView(switchR);
    }
    
    private void addButton(String str, View.OnClickListener onClickListener) {
        Button button = new Button(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -1);
        layoutParams.setMargins(0, 2, 0, 0);
        button.setLayoutParams(layoutParams);
        button.setPadding(10, 5, 10, 5);
        button.setText("♔  " + str + "  ♔");
        button.setTextColor(Color.parseColor("#000000"));
        button.setTextSize(10.0f);
        button.setScaleX(0.85f);
        button.setScaleY(0.85f);
        button.setTypeface(button.getTypeface(), Typeface.BOLD);
        button.setBackgroundColor(Color.parseColor("#FFFFFF"));
        button.setOnClickListener(onClickListener);
        this.modBody.addView(button);
    }

    private void addSeekBar(String str, int i, SeekBar.OnSeekBarChangeListener onSeekBarChangeListener) {
        TextView textView = new TextView(this);
        textView.setText(str);
        textView.setTextColor(Color.parseColor("#000000"));
        textView.setTextSize(10.0f);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-2, -2);
        textView.setLayoutParams(layoutParams);
        layoutParams.setMargins(0, 2, 0, 0);
        this.modBody.addView(textView);
        SeekBar seekBar = new SeekBar(this);
        seekBar.setMax(i);
        seekBar.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
        seekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
        this.modBody.addView(seekBar);
    }


    private void addSpacing(String string){
        TextView textView = new TextView(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -1);
        layoutParams.setMargins(0, 2, 0, 0);
        textView.setLayoutParams(layoutParams);
        textView.setGravity(1);
        textView.setText("♔  " + string + "  ♔");
        textView.setTextColor(Color.parseColor("#000000"));
        textView.setTextSize(12.0f);
        textView.setPadding(10, 5, 10, 5);
        this.modBody.addView(textView);
    }

    //For our image a little converter
    private int convertDipToPixels(float f) {
        return (int) ((f * getResources().getDisplayMetrics().density) + 0.5f);
    }

    //Check if we are still in the game. If now our Menu and Menu button will dissapear
    private boolean isNotInGame() {
        ActivityManager.RunningAppProcessInfo runningAppProcessInfo = new ActivityManager.RunningAppProcessInfo();
        ActivityManager.getMyMemoryState(runningAppProcessInfo);
        return runningAppProcessInfo.importance != 100;
    }

    private void Thread() {
        if (this.mFloatingView == null) {
            return;
        }
        if (isNotInGame()) {
            this.mFloatingView.setVisibility(View.INVISIBLE);
        } else {
            this.mFloatingView.setVisibility(View.VISIBLE);
        }
    }
}
