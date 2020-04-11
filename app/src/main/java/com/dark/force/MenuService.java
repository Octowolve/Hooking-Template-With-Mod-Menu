package com.dark.force;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.text.Html;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
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
    private ImageView imageView;
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
        if(imageView != null)
            windowManager.removeView(imageView);
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
        imageView = new ImageView(this);
        imageView.setLayoutParams(new RelativeLayout.LayoutParams(convertDipToPixels(50.0f), convertDipToPixels(50.0f)));

        try {
            //Load Image (wolve.png is the Image I use. If your button image has another name change it here)
            InputStream open = getAssets().open("wolve.png");
            imageView.setImageDrawable(Drawable.createFromStream(open, null));
            open.close();
            relativeLayout.addView(imageView);
            this.mFloatingView = relativeLayout;

            //Main UI (Our background Image for the menu. Again change the name if your background name IMage has another name)
            //InputStream open2 = getAssets().open("ghwallpaper.jpg");
            LinearLayout linearLayout = new LinearLayout(this);
            linearLayout.setLayoutParams(new LinearLayout.LayoutParams(-1, -1));
            //linearLayout.setBackground(Drawable.createFromStream(open2, null)); //if you wanna use the Image instead
            linearLayout.setBackgroundColor(Color.parseColor("#14171f"));
            linearLayout.setOrientation(LinearLayout.VERTICAL);

            //Head Text (Creates a Header text. Credit yourself, and me ples)
            TextView textView = new TextView(this);
            textView.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
            textView.setGravity(1);
            textView.setText(Html.fromHtml("Mod by Octo"));
            textView.setTextSize(20.0f);
            textView.setTextColor(Color.parseColor("#93a6ae"));

            TextView textView2 = new TextView(this);
            textView2.setLayoutParams(new LinearLayout.LayoutParams(-2, convertDipToPixels(25.0f)));
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) textView2.getLayoutParams();
            layoutParams.gravity = 17;
            layoutParams.bottomMargin = 10;
            textView2.setTextSize(15.0f);
            textView2.setText(Html.fromHtml("Fuck"));
            textView2.setTextColor(Color.parseColor("#93a6ae"));

            //Scrollview for Toggles and Main Body
            ScrollView scrollView = new ScrollView(this);
            scrollView.setLayoutParams(new LinearLayout.LayoutParams(-1, convertDipToPixels(260.0f)));
            scrollView.setScrollBarSize(convertDipToPixels(5.0f));
            scrollView.setBackgroundColor(Color.parseColor("#181c25"));
            this.modBody = new LinearLayout(this);
            this.modBody.setLayoutParams(new LinearLayout.LayoutParams(-1, -1));
            this.modBody.setOrientation(LinearLayout.VERTICAL);

            
            //Change Toggle (Due to a JNI function we can handle if a toggle has been changed)
            String[] listFT = NativeLibrary.getListFT();
            for (int i2 = 0; i2 < listFT.length; i2++) {
                final int l2 = i2;
                String str = listFT[i2];
                if (str.contains("SeekBar_")) {
                    String[] split = str.split("_");
                    addSeekBar(split[1], Integer.parseInt(split[2]), Integer.parseInt(split[3]), new SeekbarInterface() {
                        public void OnWrite(int i2) {
                            NativeLibrary.changeSeekBar(l2, i2);
                        }
                    });
                } else if(str.contains("Spinner_")){
                    String[] split = str.split("_");
                    final String[] spinnerList = getSpinnerList(split[2]);
                    addSpinner(split[1], spinnerList, new Spinner.OnItemSelectedListener(){
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            NativeLibrary.changeSpinner(l2, spinnerList[i]);
                        }
                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {

                        }
                    });
                } else if(str.contains("Spacing_")){
                    String[] split = str.split("_");
                    addSpacing(split[1]);
                }else {
                    addToggle(listFT[i2], new CompoundButton.OnCheckedChangeListener() {
                        public void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                            NativeLibrary.changeToggle(l2);
                        }
                    });
                }

            }

            addButton("Test", new CompoundButton.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(true){
                        Toast.makeText(MenuService.this, "Dropped AK47", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(MenuService.this, "Couldn't throw weapon", Toast.LENGTH_SHORT).show();
                    }
                }
            });


            //Add Body to ScrollView
            scrollView.addView(this.modBody);

            RelativeLayout relativeLayout2 = new RelativeLayout(this);
            relativeLayout2.setLayoutParams(new RelativeLayout.LayoutParams(-2, -1));
            relativeLayout2.setPadding(10, 10, 10, 10);
            relativeLayout2.setVerticalGravity(16);
            Button button = new Button(this);
            button.setBackgroundColor(Color.parseColor("#14171f"));
            button.setText("Hide");
            button.setTextColor(Color.parseColor("#93a6ae"));
            RelativeLayout.LayoutParams layoutParams2 = new RelativeLayout.LayoutParams(-2, -2);
            layoutParams2.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            button.setLayoutParams(layoutParams2);
            Button button2 = new Button(this);
            button2.setBackgroundColor(Color.parseColor("#14171f"));
            button2.setText("Kill");
            button2.setTextColor(Color.parseColor("#93a6ae"));
            relativeLayout2.addView(button);
            relativeLayout2.addView(button2);

            //Add Everything to LinearLayout
            linearLayout.addView(textView);
            linearLayout.addView(textView2);
            linearLayout.addView(scrollView);
            linearLayout.addView(relativeLayout2);
            frameLayout.addView(linearLayout);

            //Create Floating View
            final AlertDialog create = new AlertDialog.Builder(this, 2).create();
            Objects.requireNonNull(create.getWindow()).setType(i);
            create.setView(frameLayout);
            create.setCanceledOnTouchOutside(false);
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
                                imageView.setVisibility(View.INVISIBLE);
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
            button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    create.hide();
                    imageView.setVisibility(View.VISIBLE);
                }
            });
            button2.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    create.hide();
                    MenuService.this.stopSelf();
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
        switchR.setTextColor(Color.parseColor("#93a6ae"));
        switchR.setTextSize(12.0f);
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
        button.setTextColor(Color.parseColor("#93a6ae"));
        button.setTextSize(12.0f);
        button.setScaleX(0.85f);
        button.setScaleY(0.85f);
        button.setBackgroundColor(Color.parseColor("#14171f"));
        button.setOnClickListener(onClickListener);
        this.modBody.addView(button);
    }

    private void addSeekBar(final String str, final int i2, int i3, final SeekbarInterface sb) {
        final TextView textView = new TextView(this);
        textView.setText(str + ": " + i2);
        textView.setTextColor(Color.parseColor("#93a6ae"));
        textView.setTextSize(12.0f);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-2, -2);
        textView.setLayoutParams(layoutParams);
        layoutParams.setMargins(0, 2, 0, 0);
        this.modBody.addView(textView);
        SeekBar seekBar = new SeekBar(this);
        seekBar.setMax(i3);
        seekBar.setProgress(i2);
        seekBar.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            public void onProgressChanged(SeekBar seekBar, int i, boolean z) {
                if (i < i2) {
                    seekBar.setProgress(i2);
                    sb.OnWrite(i2);
                    textView.setText(str + ": " + i2);
                    return;
                }
                sb.OnWrite(i);
                textView.setText(str + ": " + i);
            }
        });
        this.modBody.addView(seekBar);
    }

    private void addSpacing(String string){
        TextView textView = new TextView(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -1);
        layoutParams.setMargins(0, 2, 0, 0);
        textView.setLayoutParams(layoutParams);
        textView.setGravity(1);
        textView.setText("♔  " + string + "  ♔");
        textView.setTextColor(Color.parseColor("#93a6ae"));
        textView.setTextSize(12.0f);
        textView.setPadding(10, 5, 10, 5);
        this.modBody.addView(textView);
    }

    private void addSpinner(String str, String[] strArr, AdapterView.OnItemSelectedListener onItemSelectedListener) {
        TextView textView = new TextView(this);
        textView.setText(str);
        textView.setTextSize(12.0f);
        textView.setTextColor(Color.parseColor("#93a6ae"));
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -2);
        textView.setLayoutParams(layoutParams);
        Spinner spinner = new Spinner(this);
        spinner.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, strArr);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerArrayAdapter);
        spinner.setOnItemSelectedListener(onItemSelectedListener);
        this.modBody.addView(textView);
        this.modBody.addView(spinner);
    }

    private String[] getSpinnerList(String type){
        switch (type){
            case "weaponsList":
                String wupons[] = {"AK", "awp"};
                return wupons;
            default:
                break;
        }
        return new String[0];
    }

    //For our image a little converter
    private int convertDipToPixels(float f) {
        return (int) ((f * getResources().getDisplayMetrics().density) + 0.5f);
    }

    //Check if we are still in the game. If now our Menu and Menu button will disappear
    private boolean isNotInGame() {
        ActivityManager.RunningAppProcessInfo runningAppProcessInfo = new ActivityManager.RunningAppProcessInfo();
        ActivityManager.getMyMemoryState(runningAppProcessInfo);
        return runningAppProcessInfo.importance != 100;
    }

    private void Thread() {
        if (this.mFloatingView != null && isNotInGame()) {
            this.mFloatingView.setVisibility(View.INVISIBLE);
        } else {
            this.mFloatingView.setVisibility(View.VISIBLE);
        }
        if (imageView != null && isNotInGame()) {
            this.imageView.setVisibility(View.INVISIBLE);
        }
    }

    private interface SeekbarInterface {
        void OnWrite(int i);
    }
}
