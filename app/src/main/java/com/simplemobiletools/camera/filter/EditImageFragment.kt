package com.simplemobiletools.camera.filter


import android.os.Bundle
import android.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import com.simplemobiletools.camera.Interface.EditImageImageFragmentListener
import com.simplemobiletools.camera.R
import kotlinx.android.synthetic.main.fragment_edit_image.*


/**
 * A simple [Fragment] subclass.
 *
 */
class EditImageFragment : Fragment(), SeekBar.OnSeekBarChangeListener {

    private var listener: EditImageImageFragmentListener?=null

    fun resetControls()
    {
        seekbar_brightness.progress = 100
        seekbar_contrast.progress = 0
        seekbar_saturation.progress = 10
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
       var progress = progress
        if (listener !=null)
        {
            if(seekBar!!.id == R.id.seekbar_brightness)
            {
                listener!!.onBrightnessChanged(progress-100)
            }

            else if(seekBar!!.id == R.id.seekbar_contrast)
            {
                progress += 10
                val floatVal = .10f*progress
                listener!!.onContrastChanged(floatVal)
            }

            else if(seekBar!!.id == R.id.seekbar_saturation)
            {
                progress +=10
                val floatVal = .10f*progress
                listener!!.onSaturationChanged(floatVal)
            }
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
        if (listener != null)
            listener!!.onEditStarted()
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
        if (listener != null)
            listener!!.onEditCompleted()
    }


    fun setListener(listener: EditImageImageFragmentListener)
    {
        this.listener = listener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_edit_image, container, false)

        seekbar_brightness.max = 200
        seekbar_brightness.progress = 100

        seekbar_contrast.max = 20
        seekbar_contrast.progress = 0

        seekbar_saturation.max = 30
        seekbar_saturation.progress = 10

        seekbar_saturation.setOnSeekBarChangeListener(this)
        seekbar_contrast.setOnSeekBarChangeListener(this)
        seekbar_brightness.setOnSeekBarChangeListener(this)

        return view
    }


}
