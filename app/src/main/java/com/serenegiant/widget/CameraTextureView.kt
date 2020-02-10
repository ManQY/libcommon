package com.serenegiant.widget
/*
 * libcommon
 * utility/helper classes for myself
 *
 * Copyright (c) 2014-2020 saki t_saki@serenegiant.com
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

import android.content.Context
import android.graphics.SurfaceTexture
import android.util.AttributeSet
import android.util.Log
import com.serenegiant.widget.CameraDelegator.ICameraView

class CameraTextureView @JvmOverloads constructor(
	context: Context?, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
		: ZoomAspectScaledTextureView(context, attrs, defStyleAttr), ICameraView {

	private val mCameraDelegator: CameraDelegator

	init {
		if (DEBUG) Log.v(TAG, "コンストラクタ:")

		mCameraDelegator = CameraDelegator(this@CameraTextureView,
			CameraDelegator.DEFAULT_PREVIEW_WIDTH, CameraDelegator.DEFAULT_PREVIEW_HEIGHT,
			object : CameraDelegator.ICameraRenderer {
				override fun hasSurface(): Boolean {
					if (DEBUG) Log.v(TAG, "hasSurface:")
					return this@CameraTextureView.hasSurface()
				}

				override fun getInputSurfaceTexture(): SurfaceTexture {
					val st = this@CameraTextureView.surfaceTexture
					if (DEBUG) Log.v(TAG, "getInputSurfaceTexture:$st")
					return st
				}

				override fun updateViewport() {
					if (DEBUG) Log.v(TAG, "updateViewport:")
				}

				override fun onPreviewSizeChanged(width: Int, height: Int) {
					if (DEBUG) Log.v(TAG, String.format("onPreviewSizeChanged:(%dx%d)", width, height))
				}
			}
		)

		register(object : SurfaceTextureListener {
			override fun onSurfaceTextureAvailable(
				surface: SurfaceTexture?, width: Int, height: Int) {

				if (DEBUG) Log.v(TAG, String.format("onSurfaceTextureAvailable:(%dx%d)",
					width, height))
				mCameraDelegator.startPreview(
					CameraDelegator.DEFAULT_PREVIEW_WIDTH, CameraDelegator.DEFAULT_PREVIEW_HEIGHT)
			}

			override fun onSurfaceTextureSizeChanged(
				surface: SurfaceTexture?,
				width: Int, height: Int) {

				if (DEBUG) Log.v(TAG, String.format("onSurfaceTextureSizeChanged:(%dx%d)",
					width, height))
			}

			override fun onSurfaceTextureDestroyed(
				surface: SurfaceTexture?): Boolean {

				if (DEBUG) Log.v(TAG, "onSurfaceTextureDestroyed:")
				return true
			}

			override fun onSurfaceTextureUpdated(
				surface: SurfaceTexture?) {

//				if (DEBUG) Log.v(TAG, "onSurfaceTextureUpdated:")
			}

		})
	}

	override fun onResume() {
		if (DEBUG) Log.v(TAG, "onResume:")
		mCameraDelegator.onResume()
	}

	override fun onPause() {
		if (DEBUG) Log.v(TAG, "onPause:")
		mCameraDelegator.onPause()
	}

	override fun setVideoSize(width: Int, height: Int) {
		if (DEBUG) Log.v(TAG, String.format("setVideoSize:(%dx%d)", width, height))
		mCameraDelegator.setVideoSize(width, height)
	}

	override fun addListener(listener: CameraDelegator.OnFrameAvailableListener) {
		if (DEBUG) Log.v(TAG, "addListener:")
	}

	override fun removeListener(listener: CameraDelegator.OnFrameAvailableListener) {
		if (DEBUG) Log.v(TAG, "removeListener:")
	}

	override fun setScaleMode(mode: Int) {
		if (DEBUG) Log.v(TAG, "setScaleMode:")
		mCameraDelegator.scaleMode = mode
	}

	override fun getScaleMode(): Int {
		if (DEBUG) Log.v(TAG, "getScaleMode:")
		return mCameraDelegator.scaleMode
	}

	override fun getVideoWidth(): Int {
		if (DEBUG) Log.v(TAG, "getVideoWidth:")
		return mCameraDelegator.width
	}

	override fun getVideoHeight(): Int {
		if (DEBUG) Log.v(TAG, "getVideoHeight:")
		return mCameraDelegator.height
	}

	companion object {
		private const val DEBUG = false // set false on production
		private val TAG = CameraTextureView::class.java.simpleName
	}
}