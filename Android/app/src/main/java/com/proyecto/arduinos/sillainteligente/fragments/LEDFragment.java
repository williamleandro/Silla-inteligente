package com.proyecto.arduinos.sillainteligente.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.proyecto.arduinos.sillainteligente.R;
import com.proyecto.arduinos.sillainteligente.utilitarios.Constante;
import com.proyecto.arduinos.sillainteligente.hilos.HiloSalida;

import java.text.DecimalFormat;

/**
 * A simple {@link Fragment} subclass.
 */
public class LEDFragment extends Fragment {
    private HiloSalida hiloSalida;
    private SeekBar seekBar;
    private TextView tvIntensidad;
    private boolean esLuzEncendida = false;

    public LEDFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View vista = inflater.inflate(R.layout.fragment_led, container, false);

        this.seekBar = vista.findViewById(R.id.seekBar);
        this.seekBar.setOnSeekBarChangeListener(cambioValorIntensidad);
        this.seekBar.setEnabled(false);

        this.tvIntensidad = vista.findViewById(R.id.tvPorcIntensidad);

        return vista;
    }

    public void setHiloSalida(HiloSalida hiloSalida) {
        this.hiloSalida = hiloSalida;
    }

    public void setFlagLuzEncendida(boolean flag) {
        this.esLuzEncendida = flag;
        this.seekBar.setEnabled(flag);
    }

    private SeekBar.OnSeekBarChangeListener cambioValorIntensidad = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            tvIntensidad.setText(progress + "%");

            DecimalFormat format = new DecimalFormat("00");
            String progreso = format.format(progress);

            hiloSalida.enviarMensaje(Constante.SEÑAL_SEEK_ARD+progreso+"\n");
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    };

}
