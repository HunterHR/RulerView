package com.migao.RulerViewDemo.ruler;


import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.*;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import com.migao.RulerViewDemo.R;

/**
 *
 * @author R.W.
 *
 */
public class RulerView extends View {

	public float UNIT;
	public float mRulerHeight;
	public float mRuleScale;
	public int mScreenW;
	public int mScreenH;
	public float mFontSize;
	public float PADDING;
	public float DISPLAY_FONT_SIZE_SMALL;

	boolean unlockLineCanvas = false;
	float lineX;
	float lineOffset;
	float startX;
	float lastX;

	private static final int PAINT_STROKE = 4;
	private static final int MIN_VALUE = 50;

	private Paint mLinePaint;
	private int mLinePaintColor;
	/**
	 * 尺子画笔
	 */
	private Paint mRulerPaint;
	private int mRulerPaintColor;

	private Paint mFontsPaint;
	private int mFontsPaintColor;

	/**
	 * 全国最低油耗
	 */
	private Paint mMinValuePaint;
	private int mMinValuePaintColor;

	/**
	 * 全国平均油耗
	 */
	private Paint mAveraryValuePaint;
	private int mAveraryValuePaintColor;
	/**
	 * 工信部标准油耗
	 */
	private Paint mStandardValuePaint;
	private int mStandardValuePaintColor;
	/**
	 * 当天平均油耗
	 */
	private Paint mCurrentValuePaint;
	private int mCurrentValuePaintColor;

	/**
	 * View的背景色
	 */
	private int mBgColor;

	private DisplayMetrics dm;

	private Context mContext;

	public RulerView(Context context) {
		super(context);

		initView(context, null, 0);
	}

	public RulerView(Context context, AttributeSet attrs) {
		super(context, attrs);

		initView(context, attrs, 0);
	}

	public RulerView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);

		initView(context, attrs, defStyleAttr);
	}

	private void initView(Context context, AttributeSet attrs, int defStyleAttr) {

		mContext = context;
		dm = new DisplayMetrics();

		WindowManager wm = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		wm.getDefaultDisplay().getMetrics(dm);

		initPaint(context, attrs, defStyleAttr);
	}

	private void initPaint(Context context, AttributeSet attrs, int defStyleAttr) {
		TypedArray arr = context.obtainStyledAttributes(attrs,
				R.styleable.MGRulerView, defStyleAttr, R.style.BaseRulerStyle);
		try {
			if (arr != null) {
				mBgColor = arr.getColor(R.styleable.MGRulerView_mBgColor,
						android.R.color.darker_gray);

				mLinePaintColor = arr.getColor(
						R.styleable.MGRulerView_mLinePaintColor,
						android.R.color.darker_gray);

				mRulerPaintColor = arr.getColor(
						R.styleable.MGRulerView_mRulerPaintColor,
						android.R.color.darker_gray);
				mFontsPaintColor = arr.getColor(
						R.styleable.MGRulerView_mFontsPaintColor,
						android.R.color.darker_gray);
				mMinValuePaintColor = arr.getColor(
						R.styleable.MGRulerView_mMinValuePaintColor,
						android.R.color.darker_gray);
				mAveraryValuePaintColor = arr.getColor(
						R.styleable.MGRulerView_mAveraryValuePaintColor,
						android.R.color.darker_gray);
				mStandardValuePaintColor = arr.getColor(
						R.styleable.MGRulerView_mStandardValuePaintColor,
						android.R.color.darker_gray);
				mCurrentValuePaintColor = arr.getColor(
						R.styleable.MGRulerView_mCurrentValuePaintColor,
						android.R.color.darker_gray);

				DISPLAY_FONT_SIZE_SMALL = TypedValue.applyDimension(
						TypedValue.COMPLEX_UNIT_DIP, 16, dm);

				UNIT = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, 0.5f,
						dm);

				mRulerHeight = TypedValue.applyDimension(
						TypedValue.COMPLEX_UNIT_DIP, 16, dm);

				mFontSize = TypedValue.applyDimension(
						TypedValue.COMPLEX_UNIT_DIP, 10, dm);

				PADDING = mFontSize / 2;

				mScreenW = dm.widthPixels;

				int dd = this.getHeight();

				mScreenH = (int) TypedValue.applyDimension(
						TypedValue.COMPLEX_UNIT_DIP, 50, dm);

				mLinePaint = new Paint();
				mLinePaint.setColor(mLinePaintColor);
				mLinePaint.setStrokeWidth(2);

				mRulerPaint = new Paint();
				mRulerPaint.setColor(mRulerPaintColor);
				mRulerPaint.setStrokeWidth(2);

				mMinValuePaint = new Paint();
				mMinValuePaint.setColor(mMinValuePaintColor);
				mMinValuePaint.setStrokeWidth(PAINT_STROKE);// 滑动线的粗度

				mAveraryValuePaint = new Paint();
				mAveraryValuePaint.setColor(mAveraryValuePaintColor);
				mAveraryValuePaint.setStrokeWidth(PAINT_STROKE);

				mStandardValuePaint = new Paint();
				mStandardValuePaint.setColor(mStandardValuePaintColor);
				mStandardValuePaint.setStrokeWidth(PAINT_STROKE);

				mCurrentValuePaint = new Paint();
				mCurrentValuePaint.setColor(mCurrentValuePaintColor);
				mCurrentValuePaint.setStrokeWidth(PAINT_STROKE);

				mFontsPaint = new Paint();
				mFontsPaint.setTextSize(mFontSize);// 刻度数字的大小
				mFontsPaint.setAntiAlias(true);
				mFontsPaint.setColor(mFontsPaintColor);
				// 属性配置
				goalValueLineX = PADDING;
			}
		} finally {
			if (arr != null) {
				arr.recycle();
			}
		}
	}

	/**
	 * 动作开始
	 *
	 * @param x
	 * @param y
	 */
	private void onTouchBegain(float x, float y) {
		lineOffset = Math.abs(x - goalValueLineX);

		if (lineOffset <= PADDING * 2) {
			//如果点击的距离不是很大，距离没有超过30
			startX = x;
			unlockLineCanvas = true;
		}
	}

	/**
	 * 动作过程
	 *
	 * @param x
	 * @param y
	 */
	private void onTouchMove(float x, float y) {
		if (unlockLineCanvas) {

			goalValueLineX = x;

			if (goalValueLineX < PADDING) {
				goalValueLineX = PADDING;
			} else if (goalValueLineX > lastX) {
				goalValueLineX = lastX;
			}
			lineX = Math.round((goalValueLineX - PADDING) / UNIT) + MIN_VALUE;
			startX = x;

			invalidate();

		}
	}

	/**
	 * 动作结束
	 *
	 * @param x
	 * @param y
	 */
	private void onTouchDone(float x, float y) {
		unlockLineCanvas = false;
		startX = -1;

		invalidate();

	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:
				onTouchDone(event.getX(), event.getY());
				break;

			case MotionEvent.ACTION_DOWN:
				onTouchBegain(event.getX(), event.getY());
				break;

			case MotionEvent.ACTION_MOVE:
				onTouchMove(event.getX(), event.getY());
				break;
		}
		return true;

	}

	/**
	 * 展示结果
	 *
	 * @param canvas
	 */
	private void drawDisplay(Canvas canvas, float goalValueLineX) {

		float currentValue = lineX / 10.0f;

		String sCurrentValue = String.valueOf(currentValue);

		Paint displayFontsPaint = new Paint();

		displayFontsPaint.setAntiAlias(true);
		displayFontsPaint.setColor(0xff666666);
		displayFontsPaint.setTextSize(DISPLAY_FONT_SIZE_SMALL);

		float mmWidth = displayFontsPaint.measureText(sCurrentValue);

		Rect rectDispaly = new Rect();
		displayFontsPaint.getTextBounds(sCurrentValue, 0, sCurrentValue.length(), rectDispaly);

		Resources res = mContext.getResources();
		Bitmap bg = BitmapFactory.decodeResource(res, R.drawable.ic_launcher);

		int width = bg.getWidth();
		int height = bg.getHeight();
		// -------------------------------------
		canvas.drawBitmap(bg, goalValueLineX - width / 2, mScreenH/2- rectDispaly.height()-mRulerHeight/2 , null);

		canvas.drawText(sCurrentValue, goalValueLineX - rectDispaly.width()/2, mScreenH / 2
				+ rectDispaly.height() * 1/2-mRulerHeight/2, displayFontsPaint);
	}
	float goalValueLineX;

	Canvas mCanvas;

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		mCanvas = canvas;

		drawRuler(canvas);

	}

	/**
	 *
	 */
	private void drawRuler(Canvas canvas) {
		try {
			// 大背景颜色
			canvas.drawColor(mBgColor);//0x00000000

			float left = PADDING;

			float MAX = mScreenW - PADDING;

			// 花四条基准线
			float minValueLineX = unitToDeviceX(mMinValue);
			float averaryValueLineX = unitToDeviceX(mAveraryValue);
			float currentValueLineX = unitToDeviceX(mCurrentValue);
			float standardValueLineX = unitToDeviceX(mStandardValue);
			float goneValueLineX = unitToDeviceX(mGoneValue);
			goalValueLineX = unitToDeviceX((int) lineX);

			drawYester(canvas,goneValueLineX);

			canvas.drawLine(minValueLineX, 0, minValueLineX, mScreenH,
					mMinValuePaint);// 画滑动的线条

			canvas.drawLine(averaryValueLineX, 0, averaryValueLineX, mScreenH,
					mAveraryValuePaint);// 画滑动的线条

			canvas.drawLine(currentValueLineX, 0, currentValueLineX, mScreenH,
					mCurrentValuePaint);// 画滑动的线条

			canvas.drawLine(standardValueLineX, 0, standardValueLineX,
					mScreenH, mStandardValuePaint);// 画滑动的线条

			canvas.drawLine(PADDING, mScreenH, MAX, mScreenH,
					mLinePaint);

			// -------------------------------------
			Resources res = mContext.getResources();
			Bitmap bg = BitmapFactory.decodeResource(res,
					R.drawable.ic_launcher);

			int width = bg.getWidth();
			int height = bg.getHeight();
			// -------------------------------------
			canvas.drawBitmap(bg, goneValueLineX - width / 2, mScreenH, null);

			for (int i = MIN_VALUE; MAX - left > 0; i++) {// 从最小的值开始

				mRuleScale = 0.5f;

				if (i % 5 == 0) {// 0,5,10,15分别代表0,0.5,1,1.5
					if ((i & 0x1) == 0) {
						// 一厘米的整数倍
						// 偶数
						mRuleScale = 1f;
						String txt = String.valueOf(i / 10);

						Rect bounds = new Rect();

						float txtWidth = mFontsPaint.measureText(txt);

						mFontsPaint.getTextBounds(txt, 0, txt.length(), bounds);

						// 150是RULE_HEIGHT
						canvas.drawText(txt, left - txtWidth / 2, mScreenH
								+ mFontSize / 2 + bounds.height(), mFontsPaint);
					} else {
						// 奇数
						mRuleScale = 0.75f;
					}
				}


				RectF rect = new RectF();

				rect.left = left - 1;

				rect.bottom = mScreenH ;

				rect.top = rect.bottom - mRulerHeight * mRuleScale;

				rect.right = left + 1;

				canvas.drawRect(rect, mRulerPaint);

				left += UNIT;
			}

			lastX = left - UNIT;

			drawDisplay(canvas, goalValueLineX);

		} catch (Exception e) {
		} finally {
			if (canvas != null) {
			}
		}
	}

	private void drawYester(Canvas canvas,float goneValue) {

		String sCurrentValue = String.valueOf(mGoneValue/10.0);

		Paint displayFontsPaint = new Paint();

		displayFontsPaint.setAntiAlias(true);
		displayFontsPaint.setColor(0xffEA69AE);//#EA69AE
		displayFontsPaint.setTextSize(DISPLAY_FONT_SIZE_SMALL);

		Rect rectDispaly = new Rect();
		displayFontsPaint.getTextBounds(sCurrentValue, 0, sCurrentValue.length(), rectDispaly);

		canvas.drawText(sCurrentValue, goneValue - rectDispaly.width()/2, mScreenH / 2
				+ rectDispaly.height() -mRulerHeight/2, displayFontsPaint);
	}

	public int getKedu() {
		return (int) goalValueLineX;
	}

	public void setKedu(int goalValueLineX) {
		this.goalValueLineX = goalValueLineX;
		invalidate();
	}

	public float getLineX() {
		return lineX;
	}

	public void setLineX(float lineX) {
		this.lineX = lineX;
		invalidate();
	}

	private int mMinValue = MIN_VALUE;
	private int mAveraryValue = MIN_VALUE;
	private int mStandardValue = MIN_VALUE;
	private int mCurrentValue = MIN_VALUE;

	private int mGoneValue = MIN_VALUE;
//	private int mGoalValue = MIN_VALUE;//改为lineX

	/**
	 *
	 * @param minValue
	 * @param averaryValue
	 * @param standardValue
	 * @param currentValue
	 */
	public void setValues(int minValue, int averaryValue, int standardValue,
						  int currentValue, int goneValue, int goalValue) {

		this.mMinValue = minValue;
		this.mAveraryValue = averaryValue;
		this.mStandardValue = standardValue;
		this.mCurrentValue = currentValue;

		this.mGoneValue = goneValue;

		lineX = goalValue;
		invalidate();

	}

	private float unitToDeviceX(int scale) {
		float lineX = .0f;
		lineX = (scale - MIN_VALUE) * UNIT + PADDING;
		return lineX;
	}
}
