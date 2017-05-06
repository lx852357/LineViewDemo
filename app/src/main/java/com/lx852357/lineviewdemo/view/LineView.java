package com.lx852357.lineviewdemo.view;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;


import com.lx852357.lineviewdemo.R;
import com.lx852357.lineviewdemo.Utils.UiUtils;

import java.util.ArrayList;
import java.util.Collections;


/**
 * Created by Michael on 2017/2/24.
 */

public class LineView extends View {
    private String TAG = "lineview";
    // X轴的间距
    private float xInstance;

    // 起始和结束的坐标点
    private int mStartX,mStartY,mEndX,mEndY;

    // 画笔
    private Paint mLinePaint;//专门画直线
    private Paint mTextPaint;//文字的画笔
    private Paint mXYPaint;//xy坐标轴的画笔
    private Paint mNumPaint;//格子里面的画笔

    // View 宽高
    private int mViewWidth;
    private int mViewHeight;

    //左右的间距
    private int marginLeft,marginRight;

    //坐标轴的X,Y总长度
    private int XLong,YLong;

    //坐标轴两个点之间的间距
    private int mDistance;

    //直线path的起始点
    private int mLineStartX;

    private ArrayList<Integer> formatedY = new ArrayList<>();

    //直线的path
    private Path linePath;

    //直线的动画
    private ValueAnimator lineAnimator;

    // 动画数值(用于控制动画状态)
    private float mAnimatorValue = 0;

    // 动效过程监听器
    private ValueAnimator.AnimatorUpdateListener mUpdateListener;
    private Animator.AnimatorListener mAnimatorListener;


    // 测量Path 并截取部分的工具
    private PathMeasure mMeasure;

    // 默认的动效周期 10s
    private final int DEFAULTDURATION = 6000;

    //下标日期的列表
    private ArrayList<Integer> mDates = new ArrayList<>();

    private boolean doAnimation = false;

    private Bitmap mBitmap;

    private Matrix mMatrix;//图片的矩阵

    private float[] pos;                // 当前点的实际位置
    private float[] tan;                // 当前点的tangent值,用于计算图片所需旋转的角度

    private float lastNum;//最后的数字

    private ProgressListener mListener;   //获取变化率的接口

    public void setProgressListener(ProgressListener listener){
        mListener = listener;
    }


    public LineView(Context context) {
        super(context);
    }

    public LineView(Context context, AttributeSet attrs) {
        super(context, attrs);
        marginLeft = UiUtils.dipToPx(context,10);
        marginRight = UiUtils.dipToPx(context,7);

        init();

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 5;       // 缩放图片
        mBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.pic_point_dialog, options);
        mMatrix = new Matrix();


    }


    private void init(){
        initPaint();
        initListener();
        initAnimator();
        initPath();

        // TODO: 2017/2/24 删除这段代码 这是加入的测试日期
        int temp = 21;
        for(int i=0;i<7;i++){
            mDates.add(temp);
            temp += 1;
        }
        pos = new float[2];
        tan = new float[2];

      //  lineAnimator.start();
    }

    //初始化画笔
    private void initPaint(){
        mLinePaint =  new Paint();
        mLinePaint.setColor(Color.parseColor("#f3630a"));
        mLinePaint.setAntiAlias(true);
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setStrokeWidth(10f);

        mTextPaint = new Paint();
        mTextPaint.setColor(Color.GRAY);
        mTextPaint.setTextSize(50);

        mXYPaint = new Paint();
        mXYPaint.setColor(Color.GRAY);
        mXYPaint.setStyle(Paint.Style.FILL);
        mXYPaint.setStrokeWidth(2f);

        mNumPaint = new Paint();
        mNumPaint.setColor(Color.WHITE);
        mNumPaint.setStyle(Paint.Style.STROKE);
        mNumPaint.setTextAlign(Paint.Align.CENTER);
        mNumPaint.setAntiAlias(true);
    }

    private void initPath(){
        linePath = new Path();
        mMeasure = new PathMeasure();
    }

    private void initListener() {
        mUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mAnimatorValue = (float) animation.getAnimatedValue();
                if(mListener != null){
                    mListener.onProgressValueChange(mAnimatorValue);
                }
                mMatrix.reset();
                invalidate();
            }
        };

        mAnimatorListener = new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                //doAnimation = false;
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        };
    }

    private void initAnimator(){
        lineAnimator = ValueAnimator.ofFloat(0, 1).setDuration(DEFAULTDURATION);
        lineAnimator.addUpdateListener(mUpdateListener);
        lineAnimator.addListener(mAnimatorListener);
    }



    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mViewHeight = h;
        mViewWidth = w;
        XLong = mViewWidth-marginLeft-marginRight;
        //Y大概占总长度的三分之一
        YLong = mViewHeight/3*2;

        mDistance = XLong/6;

//        linePath.moveTo(marginLeft,mViewHeight-YLong/6);

        ArrayList<Integer> arrInt = new ArrayList<>();
        for(int i=0;i<7;i++){
            arrInt.add((int)(Math.abs(Math.random()*200)));
        }

        //动画起始入口
        addAndFormat(arrInt);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawNet(canvas);

        if(doAnimation){
            drawLinePath(canvas);
        }

        //canvas.drawPath(linePath,mLinePaint);

    }

    private void drawNet(Canvas canvas){
        //把坐标原地移动到左下角
        //canvas.translate(0,mViewHeight);
       //横坐标X
        int startX = marginLeft;

        canvas.drawLine(startX,mViewHeight-YLong/6,startX+XLong,mViewHeight-YLong/6,mXYPaint);
        Log.i(TAG+" format",YLong+"在onDraw里的YLong");
        Log.i(TAG+" format",mViewHeight+"mViewHeigh");
        int distanceTemp = 0;
        int textDistanceTemp = 0;

        //纵坐标Y 坐标轴横坐标的起始点为marginLeft，纵坐标为marginLeft，暂时选取这两个值，横坐标的终值为marginLeft+XLong
        for(int i=0; i<=6; i++){
            canvas.drawLine(startX+distanceTemp,mViewHeight-YLong/6,startX+distanceTemp,mViewHeight-YLong,mXYPaint);
            distanceTemp += mDistance;
        }
        Log.i(TAG+" formatY",mViewHeight-YLong/6+",Y轴起始坐标");
        Log.i(TAG+ "formatY",mViewHeight-YLong+",Y轴结束坐标");


        //把path的起点定位为坐标轴的原点


        //把直线起始点定位为坐标轴的原点
        mLineStartX = marginLeft;

        for(int i=0;i<mDates.size();i++){
            canvas.drawText(mDates.get(i)+"",textDistanceTemp,mViewHeight,mTextPaint);
            textDistanceTemp += mDistance;
        }

    }

    //下面的日期
    public void addDate(ArrayList<Integer> dates){
        mDates = dates;
        invalidate();
    }

    //这里确定path的路径
    private void defLine(){
        int startX = marginLeft;
        int tempDistance = 0;
        for(int i=0; i<formatedY.size();i++){
            if(i == 0){
                linePath.moveTo(marginLeft,formatedY.get(0));
            }else {
                linePath.lineTo(startX+tempDistance,formatedY.get(i));
            }
            Log.i(TAG,"line to :"+""+startX+tempDistance+","+formatedY.get(i));
            tempDistance += mDistance;
        }
    }

    //把输入进来的值标准化为Y坐标
    public void addAndFormat(ArrayList<Integer> datas){
       // ArrayList<Integer> formateY = new ArrayList<>();
        int max = 0;
        max  = Collections.max(datas);
        lastNum = datas.get(datas.size()-1);
        Log.i(TAG+" formatY",max+",max");
        Log.i(TAG+" formatY",YLong+",YLONG");
        for(int i=0;i<datas.size();i++){
            if(datas.get(i) == 0){
                formatedY.add(mViewHeight-YLong/6);
            }else {
                formatedY.add(mViewHeight - datas.get(i)*YLong/max/6*5-YLong/6);
            }

            Log.i(TAG+" formatY",formatedY.get(i)+"Y");
            Log.i(TAG+" formatY",datas+",datas");

        }
        defLine();
        doAnimation = true;
        lineAnimator.start();
    }

    private void drawLinePath(Canvas canvas){
        mMeasure.setPath(linePath,false);
        mMeasure.getPosTan(mMeasure.getLength() * mAnimatorValue, pos, tan);

        mMatrix.postTranslate(pos[0] - mBitmap.getWidth() / 2, pos[1] - mBitmap.getHeight());

        mNumPaint.setTextSize(mBitmap.getWidth() / 3);

        Path dst = new Path();
        Path dst2 = new Path();

        //这个东西是从1画到0
        /*mMeasure.getSegment(mMeasure.getLength() * mAnimatorValue, mMeasure.getLength(), dst, true);*/

        //从0画到1
        mMeasure.getSegment(0, mMeasure.getLength() * mAnimatorValue, dst, true);
        mMeasure.getSegment(mMeasure.getLength() * mAnimatorValue-mBitmap.getWidth()/2,mMeasure.getLength() * mAnimatorValue+mBitmap.getWidth()/2,dst2,true);

        //textDst.addPath(dst2, mMatrix);

        canvas.drawPath(dst,mLinePaint);
        canvas.drawBitmap(mBitmap, mMatrix, mLinePaint);
        canvas.drawText(""+lastNum,pos[0],pos[1]-mBitmap.getHeight()/2,mNumPaint);

    }
}