package com.simplemobiletools.camera.activities

import com.simplemobiletools.camera.R
import com.simplemobiletools.camera.helpers.TextureRenderer
import com.simplemobiletools.camera.helpers.GLToolbox
import java.io.File
import java.io.FileOutputStream
import java.nio.IntBuffer
import java.util.Random
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.effect.Effect
import android.media.effect.EffectContext
import android.media.effect.EffectFactory
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.GLUtils
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import kotlinx.android.synthetic.main.activity_effectsfilter.*

class EffectsFilterActivity : Activity(), GLSurfaceView.Renderer {

    private var mEffectView: GLSurfaceView? = null
    private val mTextures = IntArray(2)
    private var mEffectContext: EffectContext? = null
    private var mEffect: Effect? = null
    private val mTexRenderer = TextureRenderer()
    private var mImageWidth: Int = 0
    private var mImageHeight: Int = 0
    private var mInitialized = false
    internal var mCurrentEffect: Int = 0
    private var saveFrame: Boolean = false
    var mPhotoByteArrayFromMain: ByteArray? = null

    fun setCurrentEffect(effect: Int) {
        mCurrentEffect = effect
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_effectsfilter)
        /**
         * Initialise the renderer and tell it to only render when Explicit
         * requested with the RENDERMODE_WHEN_DIRTY option
         */
        mEffectView = findViewById<GLSurfaceView>(R.id.effectsview)
        mEffectView!!.setEGLContextClientVersion(2)
        mEffectView!!.setRenderer(this)
        mEffectView!!.renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
        mCurrentEffect = R.id.none
        mPhotoByteArrayFromMain = intent.getByteArrayExtra("photoBitmap")
        camerareturn.setOnClickListener { returnToCamera() }
    }

    fun showPopUp(view: View) {
        val popupMenu = PopupMenu(this, view)
        val inflater = popupMenu.menuInflater
        inflater.inflate(R.menu.effectsfilter, popupMenu.menu)
        popupMenu.show()

        popupMenu.setOnMenuItemClickListener { item: MenuItem ->
            setCurrentEffect(item.itemId)
            mEffectView!!.requestRender()
            true
        }
    }

    fun returnToCamera() {
        val intent = Intent(applicationContext, MainActivity::class.java)
        startActivity(intent)
    }

    private fun loadTextures() {
        // Generate textures
        GLES20.glGenTextures(2, mTextures, 0)

        // Load input bitmap. this variable will determine the image the filters are used on
        var bmp: Bitmap?
        if (mPhotoByteArrayFromMain != null) {
            // if a photo was actually taken, import it from the intent and convert it to a bitmap
            val byteArray: ByteArray = intent.getByteArrayExtra("photoBitmap")
            bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
        } else { // else import the default image of donald duck
            bmp = BitmapFactory.decodeResource(resources, R.drawable.duck)
        }

        val bitmap = bmp
        mImageWidth = bitmap.width
        mImageHeight = bitmap.height
        mTexRenderer.updateTextureSize(mImageWidth, mImageHeight)

        // Upload to texture
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextures[0])
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)

        // Set texture parameters
        GLToolbox.initTexParams()
    }

    private fun initEffect() {
        val effectFactory = mEffectContext!!.factory
        if (mEffect != null) {
            mEffect!!.release()
        }
        /**
         * Initialize the correct effect based on the selected menu/action item
         */
        when (mCurrentEffect) {

            R.id.none -> {
            }

            R.id.autofix -> {
                mEffect = effectFactory.createEffect(EffectFactory.EFFECT_AUTOFIX)
                mEffect!!.setParameter("scale", 0.5f)
            }

            R.id.bw -> {
                mEffect = effectFactory.createEffect(EffectFactory.EFFECT_BLACKWHITE)
                mEffect!!.setParameter("black", .1f)
                mEffect!!.setParameter("white", .7f)
            }

            R.id.brightness -> {
                mEffect = effectFactory.createEffect(EffectFactory.EFFECT_BRIGHTNESS)
                mEffect!!.setParameter("brightness", 2.0f)
            }

            R.id.contrast -> {
                mEffect = effectFactory.createEffect(EffectFactory.EFFECT_CONTRAST)
                mEffect!!.setParameter("contrast", 1.4f)
            }

            R.id.crossprocess -> mEffect = effectFactory.createEffect(EffectFactory.EFFECT_CROSSPROCESS)

            R.id.documentary -> mEffect = effectFactory.createEffect(EffectFactory.EFFECT_DOCUMENTARY)

            R.id.duotone -> {
                mEffect = effectFactory.createEffect(EffectFactory.EFFECT_DUOTONE)
                mEffect!!.setParameter("first_color", Color.YELLOW)
                mEffect!!.setParameter("second_color", Color.DKGRAY)
            }

            R.id.filllight -> {
                mEffect = effectFactory.createEffect(EffectFactory.EFFECT_FILLLIGHT)
                mEffect!!.setParameter("strength", .8f)
            }

            R.id.fisheye -> {
                mEffect = effectFactory.createEffect(EffectFactory.EFFECT_FISHEYE)
                mEffect!!.setParameter("scale", .5f)
            }

            R.id.flipvert -> {
                mEffect = effectFactory.createEffect(EffectFactory.EFFECT_FLIP)
                mEffect!!.setParameter("vertical", true)
            }

            R.id.fliphor -> {
                mEffect = effectFactory.createEffect(EffectFactory.EFFECT_FLIP)
                mEffect!!.setParameter("horizontal", true)
            }

            R.id.grain -> {
                mEffect = effectFactory.createEffect(EffectFactory.EFFECT_GRAIN)
                mEffect!!.setParameter("strength", 1.0f)
            }

            R.id.grayscale -> mEffect = effectFactory.createEffect(EffectFactory.EFFECT_GRAYSCALE)

            R.id.lomoish -> mEffect = effectFactory.createEffect(EffectFactory.EFFECT_LOMOISH)

            R.id.negative -> mEffect = effectFactory.createEffect(EffectFactory.EFFECT_NEGATIVE)

            R.id.posterize -> mEffect = effectFactory.createEffect(EffectFactory.EFFECT_POSTERIZE)

            R.id.rotate -> {
                mEffect = effectFactory.createEffect(EffectFactory.EFFECT_ROTATE)
                mEffect!!.setParameter("angle", 180)
            }

            R.id.saturate -> {
                mEffect = effectFactory.createEffect(EffectFactory.EFFECT_SATURATE)
                mEffect!!.setParameter("scale", .5f)
            }

            R.id.sepia -> mEffect = effectFactory.createEffect(EffectFactory.EFFECT_SEPIA)

            R.id.sharpen -> mEffect = effectFactory.createEffect(EffectFactory.EFFECT_SHARPEN)

            R.id.temperature -> {
                mEffect = effectFactory.createEffect(EffectFactory.EFFECT_TEMPERATURE)
                mEffect!!.setParameter("scale", .9f)
            }

            R.id.tint -> {
                mEffect = effectFactory.createEffect(EffectFactory.EFFECT_TINT)
                mEffect!!.setParameter("tint", Color.MAGENTA)
            }

            R.id.vignette -> {
                mEffect = effectFactory.createEffect(EffectFactory.EFFECT_VIGNETTE)
                mEffect!!.setParameter("scale", .5f)
            }

            else -> {
            }
        }
    }

    private fun applyEffect() {
        mEffect!!.apply(mTextures[0], mImageWidth, mImageHeight, mTextures[1])
    }

    private fun renderResult() {
        if (mCurrentEffect != R.id.none) {
            // if no effect is chosen, just render the original bitmap
            mTexRenderer.renderTexture(mTextures[1])
        } else {
            saveFrame = true
            // render the result of applyEffect()
            mTexRenderer.renderTexture(mTextures[0])
        }
    }

    override fun onDrawFrame(gl: GL10) {
        if (!mInitialized) {
            // Only need to do this once
            mEffectContext = EffectContext.createWithCurrentGlContext()
            mTexRenderer.init()
            loadTextures()
            mInitialized = true
        }
        if (mCurrentEffect != R.id.none) {
            // if an effect is chosen initialize it and apply it to the texture
            initEffect()
            applyEffect()
        }
        renderResult()
        if (saveFrame) {
            saveBitmap(takeScreenshot(gl))
        }
    }

    private fun saveBitmap(bitmap: Bitmap) {
        val root = Environment.getExternalStorageDirectory().toString()
        val myDir = File(root + "/saved_images")
        myDir.mkdirs()
        val generator = Random()
        var n = 10000
        n = generator.nextInt(n)
        val fname = "Image-$n.jpg"
        val file = File(myDir, fname)
        if (file.exists()) file.delete()
        try {
            val out = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
            out.flush()
            out.close()
            Log.i("TAG", "Image SAVED==========" + file.getAbsolutePath())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun takeScreenshot(mGL: GL10): Bitmap {
        val mWidth = mEffectView!!.getWidth()
        val mHeight = mEffectView!!.getHeight()
        val ib = IntBuffer.allocate(mWidth * mHeight)
        val ibt = IntBuffer.allocate(mWidth * mHeight)
        mGL.glReadPixels(0, 0, mWidth, mHeight, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, ib)

        // Convert upside down mirror-reversed image to right-side up normal
        // image.
        for (i in 0 until mHeight) {
            for (j in 0 until mWidth) {
                ibt.put((mHeight - i - 1) * mWidth + j, ib.get(i * mWidth + j))
            }
        }

        val mBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888)
        mBitmap.copyPixelsFromBuffer(ibt)
        return mBitmap
    }

    override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
        if (mTexRenderer != null) {
            mTexRenderer.updateViewSize(width, height)
        }
    }

    override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
    }
}
