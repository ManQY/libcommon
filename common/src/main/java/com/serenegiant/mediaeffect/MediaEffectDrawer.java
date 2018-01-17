package com.serenegiant.mediaeffect;
/*
 * libcommon
 * utility/helper classes for myself
 *
 * Copyright (c) 2014-2017 saki t_saki@serenegiant.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
*/

import android.opengl.GLES20;
import android.opengl.Matrix;

import com.serenegiant.glutils.GLHelper;
import com.serenegiant.glutils.TextureOffscreen;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Locale;

import static com.serenegiant.glutils.ShaderConst.*;

public class MediaEffectDrawer {

	protected TextureOffscreen mOutputOffscreen;
	protected boolean mEnabled = true;

	private static final float[] VERTICES = { 1.0f, 1.0f, -1.0f, 1.0f, 1.0f, -1.0f, -1.0f, -1.0f };
	private static final float[] TEXCOORD = { 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f };

	private static final int FLOAT_SZ = Float.SIZE / 8;
	private static final int VERTEX_NUM = 4;
	private static final int VERTEX_SZ = VERTEX_NUM * 2;

	protected final Object mSync = new Object();
	private final int mTexTarget;
	private final int muMVPMatrixLoc;
	private final int muTexMatrixLoc;
	private final int[] muTexLoc;
	private final float[] mMvpMatrix = new float[16];
	private int hProgram;

	public MediaEffectDrawer() {
		this(1, false, VERTEX_SHADER, FRAGMENT_SHADER_2D);
	}

	public MediaEffectDrawer(final int numTex) {
		this(numTex, false, VERTEX_SHADER, FRAGMENT_SHADER_2D);
	}

	public MediaEffectDrawer(final String fss) {
		this(1, false, VERTEX_SHADER, fss);
	}

	public MediaEffectDrawer(final int numTex, final String fss) {
		this(numTex, false, VERTEX_SHADER, fss);
	}

	public MediaEffectDrawer(final boolean isOES, final String fss) {
		this(1, isOES, VERTEX_SHADER, fss);
	}

	public MediaEffectDrawer(final int numTex, final boolean isOES, final String fss) {
		this(numTex, isOES, VERTEX_SHADER, fss);
	}

	public MediaEffectDrawer(final boolean isOES, final String vss, final String fss) {
		this(1, isOES, VERTEX_SHADER, fss);
	}
	
	public MediaEffectDrawer(final int numTex,
		final boolean isOES, final String vss, final String fss) {

		mTexTarget = isOES ? GL_TEXTURE_EXTERNAL_OES : GL_TEXTURE_2D;
		final FloatBuffer pVertex = ByteBuffer.allocateDirect(VERTEX_SZ * FLOAT_SZ)
			.order(ByteOrder.nativeOrder()).asFloatBuffer();
		pVertex.put(VERTICES);
		pVertex.flip();
		final FloatBuffer pTexCoord = ByteBuffer.allocateDirect(VERTEX_SZ * FLOAT_SZ)
			.order(ByteOrder.nativeOrder()).asFloatBuffer();
		pTexCoord.put(TEXCOORD);
		pTexCoord.flip();

		// テクスチャ用のロケーションは最低でも1つは確保する
		muTexLoc = new int[numTex > 0 ? numTex : 1];
		hProgram = GLHelper.loadShader(vss, fss);
		GLES20.glUseProgram(hProgram);
		final int maPositionLoc = GLES20.glGetAttribLocation(hProgram, "aPosition");
		final int maTextureCoordLoc = GLES20.glGetAttribLocation(hProgram, "aTextureCoord");
        muMVPMatrixLoc = GLES20.glGetUniformLocation(hProgram, "uMVPMatrix");
        muTexMatrixLoc = GLES20.glGetUniformLocation(hProgram, "uTexMatrix");
        muTexLoc[0] = GLES20.glGetUniformLocation(hProgram, "sTexture");
        for (int i = 1; i < numTex; i++) {
			muTexLoc[i] = GLES20.glGetUniformLocation(hProgram,
				String.format(Locale.US, "sTexture%d", i + 1));
		}
        // モデルビュー変換行列を初期化
		Matrix.setIdentityM(mMvpMatrix, 0);
		//
		if (muMVPMatrixLoc >= 0) {
        	GLES20.glUniformMatrix4fv(muMVPMatrixLoc, 1, false, mMvpMatrix, 0);
		}
		if (muTexMatrixLoc >= 0) {
			// ここは単位行列に初期化するだけなのでmMvpMatrixを流用
        	GLES20.glUniformMatrix4fv(muTexMatrixLoc, 1, false, mMvpMatrix, 0);
		}
		// 頂点座標配列を割り当てる
		GLES20.glVertexAttribPointer(maPositionLoc, 2, GLES20.GL_FLOAT, false, VERTEX_SZ, pVertex);
		GLES20.glEnableVertexAttribArray(maPositionLoc);
		// テクスチャ座標配列を割り当てる
		GLES20.glVertexAttribPointer(maTextureCoordLoc, 2, GLES20.GL_FLOAT, false, VERTEX_SZ, pTexCoord);
		GLES20.glEnableVertexAttribArray(maTextureCoordLoc);
	}

	public void release() {
		GLES20.glUseProgram(0);
		if (hProgram >= 0) {
			GLES20.glDeleteProgram(hProgram);
		}
		hProgram = -1;
	}

	protected int getProgram() {
		return hProgram;
	}

	public float[] getMvpMatrix() {
		return mMvpMatrix;
	}

	/**
	 * このクラスでは何もしない, 必要なら下位クラスでオーバーライドすること
	 * @param width
	 * @param height
	 */
	public void setTexSize(final int width, final int height) {
	}

	/**
	 * モデルビュー変換行列に行列を割り当てる
	 * @param matrix 領域チェックしていないのでoffsetから16個以上必須
	 * @param offset
	 * @return
	 */
	public void setMvpMatrix(final float[] matrix, final int offset) {
		synchronized (mSync) {
			System.arraycopy(matrix, offset, mMvpMatrix, 0, mMvpMatrix.length);
		}
	}

	/**
	 * モデルビュー変換行列のコピーを取得
	 * @param matrix 領域チェックしていないのでoffsetから16個以上必須
	 * @param offset
	 */
	public void getMvpMatrix(final float[] matrix, final int offset) {
		System.arraycopy(mMvpMatrix, 0, matrix, offset, mMvpMatrix.length);
	}

	/**
	 * preDraw => draw => postDrawを順に呼び出す
	 * @param tex_ids texture ID
	 * @param tex_matrix テクスチャ変換行列、nullならば以前に適用したものが再利用される.領域チェックしていないのでoffsetから16個以上確保しておくこと
	 * @param offset テクスチャ変換行列のオフセット
	 */
	protected void apply(final int[] tex_ids, final float[] tex_matrix, final int offset) {
		synchronized (mSync) {
			GLES20.glUseProgram(hProgram);
			preDraw(tex_ids, tex_matrix, offset);
			draw(tex_ids, tex_matrix, offset);
			postDraw();
		}
	}

	/**
	 * 描画の前処理
	 * テクスチャ変換行列/モデルビュー変換行列を代入, テクスチャをbindする
	 * mSyncはロックされて呼び出される
	 * @param tex_ids texture ID
	 * @param tex_matrix テクスチャ変換行列、nullならば以前に適用したものが再利用される.領域チェックしていないのでoffsetから16個以上確保しておくこと
	 * @param offset テクスチャ変換行列のオフセット
	 */
	protected void preDraw(final int[] tex_ids, final float[] tex_matrix, final int offset) {
		if ((muTexMatrixLoc >= 0) && (tex_matrix != null)) {
			GLES20.glUniformMatrix4fv(muTexMatrixLoc, 1, false, tex_matrix, offset);
		}
		if (muMVPMatrixLoc >= 0) {
			GLES20.glUniformMatrix4fv(muMVPMatrixLoc, 1, false, mMvpMatrix, 0);
		}
		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		GLES20.glBindTexture(mTexTarget, tex_ids[0]);
		GLES20.glUniform1i(muTexLoc[0], 0);
		// FIXME ここでmuTexLocを使う？
	}

	/**
	 * 実際の描画実行, GLES20.glDrawArraysを呼び出すだけ
	 * mSyncはロックされて呼び出される
	 * @param tex_ids texture ID
	 * @param tex_matrix テクスチャ変換行列、nullならば以前に適用したものが再利用される.領域チェックしていないのでoffsetから16個以上確保しておくこと
	 * @param offset テクスチャ変換行列のオフセット
	 */
	protected void draw(final int[] tex_ids, final float[] tex_matrix, final int offset) {
//		if (DEBUG) Log.v(TAG, "draw");
		// これが実際の描画
		GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, VERTEX_NUM);
	}

	/**
	 * 描画後の後処理, テクスチャのunbind, プログラムをデフォルトに戻す
	 * mSyncはロックされて呼び出される
	 */
	protected void postDraw() {
		GLES20.glBindTexture(mTexTarget, 0);
        GLES20.glUseProgram(0);
	}

}
