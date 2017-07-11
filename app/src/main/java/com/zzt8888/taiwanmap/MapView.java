package com.zzt8888.taiwanmap;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Xml;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class MapView extends View {

    private Context context;

    private float scale = 0.5f;
    private float minWidth = 200;
    private float minHeight = 200;

    //svg图的实际宽高
    private float svgWidth;
    private float svgHeight;

    private List<City> mCities = new ArrayList<>();
    private int[] mColors = new int[]{Color.parseColor("#87CEEB"), Color.parseColor("#76EEC6"),
            Color.parseColor("#FF6EB4"), Color.parseColor("#FFB6C1"),
            Color.parseColor("#FF8C00"), Color.parseColor("#FF3E96"),
            Color.parseColor("#EE9572"), Color.parseColor("#EE4000")};


    public MapView(Context context) {
        this(context, null);
    }

    public MapView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }


    public MapView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        minWidth = dp2px(getContext(), 400f);
        minHeight = dp2px(getContext(), 500f);

        initMapData();
    }

    //将dp转换为像素单位
    public static float dp2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (dpValue * scale + 0.5f);
    }

    private void initMapData() {
        Observable.create(new ObservableOnSubscribe<List<City>>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<List<City>> e) throws Exception {
                initCities();
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<City>>() {
                    @Override
                    public void accept(@NonNull List<City> o) throws Exception {
                        invalidate();
                    }
                });
    }

    private void initCities() {
        int colorSize = mColors.length;
        int i = 0;

        RectF rectF = new RectF();
        float maxRight = 0;
        float maxBottom = 0;
        float left = 0;
        float top = 0;
        InputStream inputStream = null;
        try {
            inputStream = getContext().getAssets().open("taiwanhigh.xml");
            XmlPullParser xmlPullParser = Xml.newPullParser();
            xmlPullParser.setInput(inputStream, "utf-8");
            int eventType = xmlPullParser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                String name = xmlPullParser.getName();
                if (!TextUtils.isEmpty(name)) {
                    if (eventType == XmlPullParser.START_TAG) {
                        if ("path".equals(name)) {
                            String pathData = xmlPullParser.getAttributeValue(null, "pathData");
                            Path path = PathParser.createPathFromPathData(pathData);
                            String cityName = xmlPullParser.getAttributeValue(null, "name");
                            City city = new City(cityName,path, mColors[i++ / colorSize], this);
                            path.computeBounds(rectF, true);

                            maxRight = Math.max(maxRight, rectF.right);
                            maxBottom = Math.max(maxBottom, rectF.bottom);
                            left = Math.min(left, rectF.left);
                            top = Math.min(top, rectF.top);

                            mCities.add(city);
                        }
                    }
                }
                eventType = xmlPullParser.next();
            }

            svgWidth = maxRight - left;
            svgHeight = maxBottom - top;


        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);

        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        switch (widthMode) {
            case MeasureSpec.EXACTLY:
                Log.d("MapView", "widthMode : MeasureSpec.EXACTLY");
                width = Math.max(width, (int) minWidth);
                break;
            case MeasureSpec.AT_MOST:
                Log.d("MapView", "widthMode : MeasureSpec.AT_MOST");
                width = (int) minWidth;
                break;
            case MeasureSpec.UNSPECIFIED:
            default:
                break;
        }

        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        switch (heightMode) {
            case MeasureSpec.EXACTLY:
                Log.d("MapView", "heightMode : MeasureSpec.EXACTLY");
                height = Math.max(height, (int) minHeight);
                break;
            case MeasureSpec.AT_MOST:
                Log.d("MapView", "heightMode : MeasureSpec.AT_MOST");
            default:
                height = (int) minHeight;
        }


//        计算需要缩放的比例
        scale =  width / svgWidth;
        scale = Math.min( height / svgHeight, scale);

        setMeasuredDimension(width, height);
    }


    @Override
    protected void onDraw(Canvas canvas) {

        for (City city : mCities) {
            canvas.save();
            canvas.scale(scale, scale);
            city.onDraw(canvas);
            canvas.restore();
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                for (City city : mCities) {
                    float x = event.getX();
                    float y = event.getY();
                    x = x / scale;
                    y = y / scale;
                    boolean isContain = city.isContainXY(x, y);
                    city.setSelected(isContain);
                    city.animationPath(isContain);
                    if (isContain){
                        String cityName = city.getName();
                        Toast.makeText(getContext(),cityName,Toast.LENGTH_SHORT).show();
                    }
                }
                invalidate();
                break;
            default:
                break;
        }
        return true;
    }
}
