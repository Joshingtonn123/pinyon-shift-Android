package com.pinyonshift.fh1;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

/** Compact native launcher and local settings for Pinyon Shift on Android. */
public final class PinyonShiftLauncherActivity extends Activity {
    public static final String PREFERENCES = "pinyon_shift_launcher_settings";
    private static final int[][] OUTPUT_SIZES = {
            {800, 600}, {720, 480}, {1280, 720}, {1280, 1024}
    };
    private static final String[] OUTPUT_LABELS = {
            "800x600", "720x480", "1280x720", "1280x1024"
    };

    public static boolean isSupportedOutputResolution(int width, int height) {
        for (int[] size : OUTPUT_SIZES) {
            if (size[0] == width && size[1] == height) return true;
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Color.rgb(8, 12, 24));
        getWindow().setNavigationBarColor(Color.rgb(8, 12, 24));

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(Color.rgb(8, 12, 24));

        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setGravity(Gravity.CENTER_HORIZONTAL);
        page.setPadding(dp(22), dp(28), dp(22), dp(24));
        scroll.addView(page, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        TextView eyebrow = text("ANDROID PREVIEW  •  FH1", 12, 0xFF52E4D2, true);
        eyebrow.setLetterSpacing(0.12f);
        page.addView(eyebrow, matchWrap());

        TextView title = text("PINYON SHIFT", 32, Color.WHITE, true);
        title.setTypeface(Typeface.create("sans-serif-black", Typeface.BOLD));
        title.setPadding(0, dp(8), 0, 0);
        page.addView(title, matchWrap());

        TextView subtitle = text("Forza Horizon • Native ARM64 build", 14, 0xFF9EACC8, false);
        subtitle.setPadding(0, dp(4), 0, dp(22));
        page.addView(subtitle, matchWrap());

        LinearLayout outputCard = card();
        page.addView(outputCard, matchWrap());
        outputCard.addView(text("OUTPUT RESOLUTION", 13, 0xFF52E4D2, true), matchWrap());
        TextView outputHint = text(
                "Choose the game's presentation buffer size. Changes apply on the next launch.",
                13, 0xFFB9C4D8, false);
        outputHint.setPadding(0, dp(6), 0, dp(12));
        outputCard.addView(outputHint, matchWrap());

        Spinner outputSpinner = new Spinner(this);
        outputSpinner.setAdapter(new OutputAdapter(this));
        outputSpinner.setSelection(selectedOutputIndex());
        outputCard.addView(outputSpinner, matchWrap());
        outputSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view,
                                       int position, long id) {
                int[] size = OUTPUT_SIZES[position];
                getSharedPreferences(PREFERENCES, MODE_PRIVATE).edit()
                        .putInt("output_width", size[0])
                        .putInt("output_height", size[1])
                        .apply();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) { }
        });

        LinearLayout internalCard = card();
        LinearLayout.LayoutParams cardParams = matchWrap();
        cardParams.topMargin = dp(14);
        page.addView(internalCard, cardParams);
        internalCard.addView(text("INTERNAL RENDER SCALE", 13, 0xFF52E4D2, true), matchWrap());

        TextView internalValue = text("1.00x  •  Native / stable", 18, Color.WHITE, true);
        internalValue.setPadding(0, dp(10), 0, dp(5));
        internalCard.addView(internalValue, matchWrap());
        TextView internalHint = text(
                "Fractional 0.25x–0.75x options need renderer-level changes: the current texture "
                        + "cache uses integer render scales and clamps Android to 1x. The 2x path "
                        + "can allocate a dedicated resolve buffer around 2 GB, so unsupported "
                        + "scales are not exposed as if they worked.",
                13, 0xFFB9C4D8, false);
        internalHint.setLineSpacing(dp(2), 1.0f);
        internalHint.setPadding(0, dp(3), 0, 0);
        internalCard.addView(internalHint, matchWrap());

        Button play = new Button(this);
        play.setText("PLAY FORZA HORIZON");
        play.setTextColor(Color.WHITE);
        play.setTextSize(15);
        play.setAllCaps(false);
        play.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        GradientDrawable playBackground = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT, new int[] {0xFF5C35DD, 0xFF147FEA});
        playBackground.setCornerRadius(dp(15));
        play.setBackground(playBackground);
        play.setPadding(dp(14), dp(7), dp(14), dp(7));
        LinearLayout.LayoutParams playParams = matchWrap();
        playParams.topMargin = dp(18);
        page.addView(play, playParams);
        play.setOnClickListener(v -> startActivity(new Intent(
                PinyonShiftLauncherActivity.this, PinyonShiftActivity.class)));

        TextView footnote = text(
                "Requires a locally built Android package and your own game data. "
                        + "Mali G90T compatibility has not been validated yet.",
                12, 0xFF8795AF, false);
        footnote.setGravity(Gravity.CENTER);
        footnote.setPadding(dp(4), dp(16), dp(4), 0);
        page.addView(footnote, matchWrap());
        setContentView(scroll);
    }

    private int selectedOutputIndex() {
        android.content.SharedPreferences preferences =
                getSharedPreferences(PREFERENCES, MODE_PRIVATE);
        int width = preferences.getInt("output_width", 1280);
        int height = preferences.getInt("output_height", 720);
        for (int i = 0; i < OUTPUT_SIZES.length; i++) {
            if (OUTPUT_SIZES[i][0] == width && OUTPUT_SIZES[i][1] == height) return i;
        }
        return 2;
    }

    private LinearLayout card() {
        LinearLayout result = new LinearLayout(this);
        result.setOrientation(LinearLayout.VERTICAL);
        result.setPadding(dp(17), dp(17), dp(17), dp(17));
        GradientDrawable background = new GradientDrawable();
        background.setColor(0xFF121B2E);
        background.setCornerRadius(dp(18));
        background.setStroke(dp(1), 0xFF293956);
        result.setBackground(background);
        return result;
    }

    private TextView text(String value, float size, int color, boolean bold) {
        TextView result = new TextView(this);
        result.setText(value);
        result.setTextSize(size);
        result.setTextColor(color);
        result.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        if (bold) result.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        return result;
    }

    private LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private final class OutputAdapter extends ArrayAdapter<String> {
        OutputAdapter(Context context) {
            super(context, android.R.layout.simple_spinner_item, OUTPUT_LABELS);
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return styleRow(position, convertView, false);
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            return styleRow(position, convertView, true);
        }

        private View styleRow(int position, View convertView, boolean dropdown) {
            TextView row = convertView instanceof TextView
                    ? (TextView) convertView : new TextView(PinyonShiftLauncherActivity.this);
            row.setText(getItem(position));
            row.setTextSize(16);
            row.setTextColor(Color.WHITE);
            row.setPadding(dp(13), dp(12), dp(13), dp(12));
            if (dropdown) row.setBackgroundColor(0xFF18243A);
            return row;
        }
    }
}
