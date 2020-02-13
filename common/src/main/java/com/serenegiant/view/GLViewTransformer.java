package com.serenegiant.view;

import android.graphics.PointF;
import android.opengl.Matrix;
import android.util.Log;

import com.serenegiant.graphics.MatrixUtils;
import com.serenegiant.widget.GLView;

import java.util.Arrays;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Size;

/**
 * GLViewのトランスフォーム処理用ヘルパークラス
 */
public class GLViewTransformer {
	private static final boolean DEBUG = false;	// TODO for debugging
	private static final String TAG = GLViewTransformer.class.getSimpleName();

	private final GLView mTargetView;
	private final float[] mTransform = new float[16];
	private final float[] mDefaultTransform = new float[16];
	private final float[] work = new float[16];

	/**
	 * 平行移動量
	 */
	private float mCurrentTransX, mCurrentTransY;
	/**
	 * 拡大縮小率
	 */
	private float mCurrentScaleX, mCurrentScaleY;
	/**
	 * 回転角度
	 */
	private float mCurrentRotate;

	/**
	 * コンストラクタ
	 * @param view
	 */
	public GLViewTransformer(@NonNull final GLView view) {
		if (DEBUG) Log.v(TAG, "コンストラクタ:");
		mTargetView = view;
	}

	@NonNull
	public GLView getTargetView() {
		return mTargetView;
	}

	/**
	 * ViewContentTransformerで保持しているトランスフォームマトリックスを
	 * ターゲットViewに設定されているトランスフォームマトリックスに設定する
	 * @param setAsDefault 設定したトランスフォームマトリックスをデフォルトにトランスフォームマトリックスとして使うかどうか
	 * @return
	 */
	public GLViewTransformer updateTransform(final boolean setAsDefault) {
		getTargetView().getTransform(mTransform);
		if (setAsDefault) {
			System.arraycopy(mTransform, 0, mDefaultTransform, 0, 16);
			// mDefaultTranslateからの相対値なのでtranslate/scale/rotateをクリアする
			if (DEBUG) Log.v(TAG, "updateTransform:default="
				+ MatrixUtils.toGLMatrixString(mDefaultTransform));
			resetValues();
		} else {
			calcValues(mTransform);
		}
		if (DEBUG) Log.v(TAG, "updateTransform:" + setAsDefault
			+ "," + MatrixUtils.toGLMatrixString(mTransform));
		return this;
	}

	protected void internalSetTransform(@Nullable final float[] transform) {
		if (DEBUG) Log.v(TAG, "internalSetTransform:" + MatrixUtils.toGLMatrixString(transform));
		getTargetView().setTransform(transform);
	}

	/**
	 * トランスフォームマトリックスを設定する
	 * @param transform nullまたは要素数が16未満なら単位行列が設定される
	 */
	public final void setTransform(@Nullable @Size(min=16) final float[] transform) {
		if (DEBUG) Log.v(TAG, "setTransform:" + Arrays.toString(transform));
		if ((transform != null) && (transform.length >= 16)) {
			System.arraycopy(transform, 0, mTransform, 0, 16);
		} else {
			Matrix.setIdentityM(mTransform, 0);
		}
		internalSetTransform(mTransform);
		calcValues(mTransform);
	}

	/**
	 * トランスフォームマトリックスのコピーを取得
	 * @param transform nullまたは要素数が9未満なら内部で新しいfloat配列を生成して返す, そうでなければコピーする
	 * @param transform
	 * @return
	 */
	public float[] getTransform(@Nullable final float[] transform) {
		float[] result = transform;
		if ((result == null) && (transform.length < 16)) {
			result = new float[16];
		}
		System.arraycopy(mTransform, 0, result, 0, 16);
		return result;
	}

	/**
	 * デフォルトのトランスフォームマトリックスを設定
	 * @param transform
	 * @return
	 */
	public GLViewTransformer setDefault(@NonNull @Size(min=16) final float[] transform) {
		System.arraycopy(transform, 0, mDefaultTransform, 0, 16);
		return this;
	}

	/**
	 * トランスフォームマトリックスを初期状態に戻す
	 * #setDefaultで変更していなけれあコンストラクタ実行時の
	 * ターゲットViewのトランスフォームマトリックスに戻る
	 */
	public void reset() {
		if (DEBUG) Log.v(TAG, "reset:");
		setTransform(mDefaultTransform);
	}

	/**
	 * 指定位置に移動
	 * @param x
	 * @param y
	 * @return
	 */
	public GLViewTransformer setTranslate(final float x, final float y) {
		if (DEBUG) Log.v(TAG, String.format("setTranslate:(%f,%f)", x, y));
		return setTransform(x, y,
			mCurrentScaleX, mCurrentScaleY,
			mCurrentRotate);
	}

	/**
	 * 現在位置からオフセット
	 * @param dx
	 * @param dy
	 * @return
	 */
	public GLViewTransformer translate(final float dx, final float dy) {
		if (DEBUG) Log.v(TAG, String.format("translate:(%f,%f)", dx, dy));
		return setTransform(mCurrentTransX + dx, mCurrentTransY + dy,
			mCurrentScaleX, mCurrentScaleY,
			mCurrentRotate);
	}

	/**
	 * 移動量を取得
	 * @param tr
	 * @return
	 */
	public PointF getTranslate(@Nullable final PointF tr) {
		if (tr != null) {
			tr.set(mCurrentTransX, mCurrentTransY);
			return tr;
		} else {
			return new PointF(mCurrentTransX, mCurrentTransY);
		}
	}

	/**
	 * 移動量を取得
	 * @return
	 */
	public float getTranslateX() {
		return mCurrentTransX;
	}

	/**
	 * 移動量を取得
	 * @return
	 */
	public float getTranslateY() {
		return mCurrentTransY;
	}

	/**
	 * 指定倍率に拡大縮小
	 * @param scaleX
	 * @param scaleY
	 * @return
	 */
	public GLViewTransformer setScale(final float scaleX, final float scaleY) {
		if (DEBUG) Log.v(TAG, String.format("setScale:(%f,%f)", scaleX, scaleY));
		return setTransform(mCurrentTransX, mCurrentTransY,
			scaleX, scaleY,
			mCurrentRotate);
	}

	/**
	 * 指定倍率に拡大縮小
	 * @param scale
	 * @return
	 */
	public GLViewTransformer setScale(final float scale) {
		if (DEBUG) Log.v(TAG, String.format("setScale:(%f)", scale));
		return setTransform(mCurrentTransX, mCurrentTransY,
			scale, scale,
			mCurrentRotate);
	}

	/**
	 * 現在の倍率から拡大縮小
	 * @param scaleX
	 * @param scaleY
	 * @return
	 */
	public GLViewTransformer scale(final float scaleX, final float scaleY) {
		if (DEBUG) Log.v(TAG, String.format("scale:(%f,%f)", scaleX, scaleY));
		return setTransform(mCurrentTransX, mCurrentTransY,
			mCurrentScaleX * scaleX, mCurrentScaleY * scaleY,
			mCurrentRotate);
	}

	/**
	 * 現在の倍率から拡大縮小
	 * @param scale
	 * @return
	 */
	public GLViewTransformer scale(final float scale) {
		if (DEBUG) Log.v(TAG, String.format("scale:(%f)", scale));
		return setTransform(mCurrentTransX, mCurrentTransY,
			mCurrentScaleX * scale, mCurrentScaleY * scale,
			mCurrentRotate);
	}

	/**
	 * 現在の拡大縮小率(横方向)を取得
	 * @return
	 */
	public float getScaleX() {
		return mCurrentScaleX;
	}

	/**
	 * 現在の拡大縮小率(縦方向)を取得
 	 * @return
	 */
	public float getScaleY() {
		return mCurrentScaleY;
	}

	/**
	 * 縦横の拡大縮小率のうち小さい方を取得
	 * @return
	 */
	public float getScale() {
		return Math.min(mCurrentScaleX, mCurrentScaleY);
	}

	/**
	 * 指定角度に回転
	 * @param degrees
	 * @return
	 */
	public GLViewTransformer setRotate(final float degrees) {
		if (DEBUG) Log.v(TAG, String.format("setRotate:(%f)", degrees));
		return setTransform(mCurrentTransX, mCurrentTransY,
			mCurrentScaleX, mCurrentScaleY,
			degrees);
	}

	/**
	 * 現在の回転角度から回転
	 * @param degrees
	 * @return
	 */
	public GLViewTransformer rotate(final float degrees) {
		if (DEBUG) Log.v(TAG, String.format("rotate:(%f)", degrees));
		return setTransform(mCurrentTransX, mCurrentTransY,
			mCurrentScaleX, mCurrentScaleY,
			mCurrentRotate + degrees);
	}

	/**
	 * 現在の回転角度[度]を取得
	 * @return
	 */
	public float getRotation() {
		return mCurrentRotate;
	}

	/**
	 * 指定した座標配列をトランスフォームマトリックスで変換する
	 * @param points
	 */
	public void mapPoints(@NonNull final float[] points) {
		throw new UnsupportedOperationException();
	}

	/**
	 * 指定した座標配列をトランスフォームマトリックスで変換する
	 * @param dst 代入先の座標配列(x,y ペア)
	 * @param src 変換元の座標配列(x,y ペア)
	 */
	public void mapPoints(@NonNull final float[] dst, @NonNull final float[] src) {
		throw new UnsupportedOperationException();
	}

	/**
	 * トランスフォームマトリックスを設定
	 * @param transX
	 * @param transY
	 * @param scaleX
	 * @param scaleY
	 * @param degrees
	 * @return
	 */
	protected GLViewTransformer setTransform(
		final float transX, final float transY,
		final float scaleX, final float scaleY,
		final float degrees) {

		if ((mCurrentTransX != transX) || (mCurrentTransY != transY)
			|| (mCurrentScaleX != scaleX) || (mCurrentScaleY != scaleY)
			|| (mCurrentRotate != degrees)) {

			mCurrentScaleX = scaleX;
			mCurrentScaleY = scaleY;
			mCurrentTransX = transX;
			mCurrentTransY = transY;
			mCurrentRotate = degrees;
			if (degrees != Float.MAX_VALUE) {
				while (mCurrentRotate > 360) {
					mCurrentRotate -= 360;
				}
				while (mCurrentRotate < -360) {
					mCurrentRotate += 360;
				}
			}

			final int w2 = mTargetView.getWidth() >> 1;
			final int h2 = mTargetView.getHeight() >> 1;
			Matrix.setIdentityM(mTransform, 0);
			// 回転 → 拡大縮小 → 平行移動 → デフォルト
			// デフォルトトランスフォームマトリックスを適用
			Matrix.multiplyMM(mTransform, 0, mTransform, 0, mDefaultTransform, 0);
			// 平行移動
			Matrix.translateM(mTransform, 0, transX, transY, 0.0f);
			// 拡大縮小(たぶんこれじゃだめ、画面中心を原点にして回転させてから元に戻す？)
			Matrix.scaleM(mTransform, 0, scaleX, scaleY, 1.0f);
			// 回転
			if (degrees != Float.MAX_VALUE) {
				Matrix.rotateM(mTransform, 0, mCurrentRotate, 0.0f, 0.0f, 1.0f);
			}
			// apply to target view
			internalSetTransform(mTransform);
		}
		return this;
	}

	/**
	 * Matrixからtranslate/scale/rotateの値を計算する
	 * @param transform
	 * @return
	 */
	protected void calcValues(@NonNull @Size(min=16) final float[] transform) {
		if (DEBUG) Log.v(TAG, "calcValues:" + MatrixUtils.toGLMatrixString(transform));
//		mTransform.getValues(work);
//		mCurrentTransX = work[Matrix.MTRANS_X];
//		mCurrentTransY = work[Matrix.MTRANS_Y];
//		mCurrentScaleX = work[Matrix.MSCALE_X];
//		mCurrentScaleY = MatrixUtils.getScale(work);
//		mCurrentRotate = MatrixUtils.getRotate(work);
		if (DEBUG) Log.v(TAG, String.format("calcValues:tr(%fx%f),scale(%f,%f),rot=%f",
			mCurrentTransX, mCurrentTransY,
			mCurrentScaleX, mCurrentScaleY,
			mCurrentRotate));
	}

	/**
	 * translate/scale/rotateの値をデフォルト(トランスフォームマトリックスとして単位行列)
	 */
	protected void resetValues() {
		mCurrentTransX = mCurrentTransY = 0.0f;
		mCurrentScaleX = mCurrentScaleY = 1.0f;
		mCurrentRotate = 0.0f;
	}
}