package com.dark.force;

import android.animation.ArgbEvaluator;
import android.animation.TimeAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.PorterDuff;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.text.Html;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
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
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class MenuService extends Service {


    public View mFloatingView;
    private LinearLayout modBody;
    private WindowManager windowManager;
    private RelativeLayout relativeLayoutImage;
    private ImageView imageView;
    public IBinder onBind(Intent intent) {
        return null;
    }
    private static GradientDrawable gd = new GradientDrawable();

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
            relativeLayoutImage.removeView(imageView);
        Toast.makeText(getBaseContext(), "MenuService Stopped", Toast.LENGTH_SHORT).show();
    }


    //When this Class is called the code in this function will be executed
    public void onCreate() {
        super.onCreate();
        //Init Lib
        NativeLibrary.init(this);
        //Create our Menu
        CreateMenu();
        //Start the Gradient Animation
        startAnimation();
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
        relativeLayoutImage = new RelativeLayout(this); //Floatin Button
        relativeLayoutImage.setLayoutParams(new RelativeLayout.LayoutParams(-1, -1));

        //Our ImageView handler which will be used for our Open/Closed button
        imageView = new ImageView(this);
        imageView.setLayoutParams(new RelativeLayout.LayoutParams(convertDipToPixels(75.0f), convertDipToPixels(75.0f)));

        try {
            //Load Image (PiinLogoRed.png is the Image I use. If your button image has another name change it here)
            InputStream open = getAssets().open("PiinLogoRed.png");
            imageView.setImageDrawable(Drawable.createFromStream(open, null));
            open.close();
            relativeLayoutImage.addView(imageView);
            this.mFloatingView = relativeLayoutImage;

            //Main UI (Our background Image for the menu. Again change the name if your background name IMage has another name)
            //InputStream open2 = getAssets().open("ghwallpaper.jpg");
            LinearLayout linearLayout = new LinearLayout(this);
            linearLayout.setLayoutParams(new LinearLayout.LayoutParams(-1, -1));
            //linearLayout.setBackground(Drawable.createFromStream(open2, null)); //if you wanna use the Image instead
            linearLayout.setBackgroundColor(Color.parseColor("#14171f"));
            //linearLayout.setBackground(gd);
            linearLayout.setOrientation(LinearLayout.VERTICAL);

            //Head Text (Creates a Header text. Credit yourself, and me ples)
            TextView modHeading = new TextView(this);
            int start = Color.parseColor("#009FFF");
            int end = Color.parseColor("#ec2F4B");
            Shader shader = new LinearGradient(0, 0, 40, modHeading.getLineHeight(),
                    start, end, Shader.TileMode.MIRROR);
            modHeading.getPaint().setShader(shader);
            modHeading.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
            modHeading.setGravity(1);
            modHeading.setText(Html.fromHtml("Mod by Octo"));
            //textView.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
            modHeading.setTextSize(20.0f);
            //textView.setTextColor(Color.parseColor("#93a6ae"));

            TextView modSubHeading = new TextView(this);
            modSubHeading.setLayoutParams(new LinearLayout.LayoutParams(-2, convertDipToPixels(25.0f)));
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) modSubHeading.getLayoutParams();
            layoutParams.gravity = 17;
            layoutParams.bottomMargin = 10;
            modSubHeading.setTextSize(15.0f);
            modSubHeading.setText(Html.fromHtml("Fuck"));
            modSubHeading.setTextColor(Color.parseColor("#93a6ae"));

            //Scrollview for Toggles and Main Body
            ScrollView scrollView = new ScrollView(this);
            scrollView.setLayoutParams(new LinearLayout.LayoutParams(-1, convertDipToPixels(260.0f)));
            scrollView.setScrollBarSize(convertDipToPixels(5.0f));
            //scrollView.setBackgroundColor(Color.parseColor("#181c25"));
            scrollView.setBackground(gd);

            this.modBody = new LinearLayout(this);
            this.modBody.setLayoutParams(new LinearLayout.LayoutParams(-1, -1));
            this.modBody.setOrientation(LinearLayout.VERTICAL);

            
            //Change Toggle (Due to a JNI function we can handle if a toggle has been changed)
            String[] listFT = NativeLibrary.getListFT();
            for (int i2 = 0; i2 < listFT.length; i2++) {
                final int l2 = i2;
                String str = listFT[i2];
                String[] split = str.split("_");
                if (str.contains("SeekBar_")) {
                    addSeekBar(split[1], Integer.parseInt(split[2]), Integer.parseInt(split[3]), new SeekbarInterface() {
                        public void OnWrite(int i2) {
                            NativeLibrary.changeSeekBar(l2, i2);
                        }
                    });
                } else if(str.contains("Spinner_")){
                    final String[] spinnerList = getSpinnerList(split[2]);
                    addSpinner(split[1], spinnerList, new Spinner.OnItemSelectedListener(){
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            ((TextView) adapterView.getChildAt(0)).setTextColor(Color.parseColor("#93a6ae"));
                            ((TextView) adapterView.getChildAt(0)).setPadding(1,1,1,1);
                            NativeLibrary.changeSpinner(l2, spinnerList[i]);
                        }
                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {

                        }
                    });
                } else if(str.contains("Spacing_")){
                    addSpacing(split[1]);
                } else if (str.contains("EditText_")) {
                    addTextField(split[1], split[2], l2);
                } else {
                    addToggle(listFT[i2], new CompoundButton.OnCheckedChangeListener() {
                        public void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                            NativeLibrary.changeToggle(l2);
                        }
                    });
                }

            }


            //Add Body to ScrollView
            scrollView.addView(this.modBody);

            RelativeLayout relativeLayout2 = new RelativeLayout(this);
            relativeLayout2.setLayoutParams(new RelativeLayout.LayoutParams(-2, -1));
            relativeLayout2.setPadding(10, 10, 10, 10);
            relativeLayout2.setVerticalGravity(16);

            Button hideButton = new Button(this);
            hideButton.setBackgroundColor(Color.parseColor("#14171f"));
            hideButton.setText("Hide");
            hideButton.setTextColor(Color.parseColor("#93a6ae"));

            RelativeLayout.LayoutParams layoutParams2 = new RelativeLayout.LayoutParams(-2, -2);
            layoutParams2.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            hideButton.setLayoutParams(layoutParams2);

            Button killButton = new Button(this);
            killButton.setBackgroundColor(Color.parseColor("#14171f"));
            killButton.setText("Kill");
            killButton.setTextColor(Color.parseColor("#93a6ae"));
            relativeLayout2.addView(hideButton);
            relativeLayout2.addView(killButton);

            //Add Everything to LinearLayout
            linearLayout.addView(modHeading);
            linearLayout.addView(modSubHeading);
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
            hideButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    create.hide();
                    imageView.setVisibility(View.VISIBLE);
                }
            });
            killButton.setOnClickListener(new View.OnClickListener() {
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

        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(-1, -1));
        linearLayout.setPadding(10, 5, 10, 5);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setGravity(17);

        Spinner spinner = new Spinner(this);
        LinearLayout linearLayout2 = new LinearLayout(this);
        linearLayout2.setLayoutParams(new LinearLayout.LayoutParams(-1, -1));
        linearLayout2.setOrientation(LinearLayout.VERTICAL);
        spinner.setLayoutParams(linearLayout2.getLayoutParams());
        spinner.getBackground().setColorFilter(1, PorterDuff.Mode.SRC_ATOP);

        CustomSpinnerAdapter adapter = new CustomSpinnerAdapter(
                this,
                android.R.layout.simple_spinner_item,
                Arrays.asList(strArr)
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(onItemSelectedListener);

        linearLayout.addView(textView);
        linearLayout2.addView(spinner);
        this.modBody.addView(linearLayout);
        this.modBody.addView(linearLayout2);
    }

    private void addTextField(String str, String hint, final int id){
        RelativeLayout relativeLayout2 = new RelativeLayout(this);
        relativeLayout2.setLayoutParams(new RelativeLayout.LayoutParams(-2, -1));
        relativeLayout2.setPadding(10, 10, 10, 10);
        relativeLayout2.setVerticalGravity(16);

        final EditText editText = new EditText(this);
        editText.setHint(hint);
        editText.setMaxLines(1);
        editText.setWidth(convertDipToPixels(250.0f));
        editText.setTextColor(Color.parseColor("#93a6ae"));
        editText.setTextSize(13.0f);
        editText.setHintTextColor(Color.parseColor("#434d52"));
        //Ok we have to set a button next to it so we know when the user is finished with the typing.
        //Makes sense xDD
        RelativeLayout.LayoutParams layoutParams2 = new RelativeLayout.LayoutParams(-2, -2);
        layoutParams2.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

        Button button2 = new Button(this);
        button2.setLayoutParams(layoutParams2);
        button2.setBackgroundColor(Color.parseColor("#14171f"));
        button2.setText(str);
        button2.setTextColor(Color.parseColor("#93a6ae"));
        button2.setTextSize(12.0f);
        button2.setScaleX(0.85f);
        button2.setScaleY(0.85f);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NativeLibrary.changeEditText(id, editText.getText().toString());
            }
        });

        relativeLayout2.addView(editText);
        relativeLayout2.addView(button2);
        this.modBody.addView(relativeLayout2);
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

    public static void startAnimation() {
        final int start = Color.parseColor("#c31432");
        final int end = Color.parseColor("#240b36");

        final ArgbEvaluator evaluator = new ArgbEvaluator();
        gd.setCornerRadius(0f);
        gd.setOrientation(GradientDrawable.Orientation.TL_BR);
        final GradientDrawable gradient = gd;

        ValueAnimator animator = TimeAnimator.ofFloat(0.0f, 1.0f);
        animator.setDuration(3000);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                Float fraction = valueAnimator.getAnimatedFraction();
                int newStrat = (int) evaluator.evaluate(fraction, start, end);
                int newEnd = (int) evaluator.evaluate(fraction, end, start);
                int[] newArray = {newStrat, newEnd};
                gradient.setColors(newArray);
            }
        });

        animator.start();
    }


    private interface SeekbarInterface {
        void OnWrite(int i);
    }

    private static class CustomSpinnerAdapter extends ArrayAdapter<String> {
        private CustomSpinnerAdapter(Context context, int resource, List<String> items) {
            super(context, resource, items);
        }

        // Affects default (closed) state of the spinner
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView view = (TextView) super.getView(position, convertView, parent);
            return view;
        }

        // Affects opened state of the spinner
        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            TextView view = (TextView) super.getDropDownView(position, convertView, parent);
            view.setTextColor(Color.parseColor("#93a6ae"));
            view.setBackgroundColor(Color.parseColor("#14171f"));
            return view;
        }
    }
}
