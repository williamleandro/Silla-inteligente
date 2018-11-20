package com.proyecto.arduinos.sillainteligente.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.proyecto.arduinos.sillainteligente.R;
import com.proyecto.arduinos.sillainteligente.utilitarios.Constante;
import com.proyecto.arduinos.sillainteligente.hilos.HiloSalida;

/**
 * A simple {@link Fragment} subclass.
 */
public class LEDFragment extends Fragment {
    private Switch aSwitch;
    private HiloSalida hiloSalida;

    public LEDFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View vista = inflater.inflate(R.layout.fragment_led, container, false);

        this.aSwitch = vista.findViewById(R.id.switch2);
        this.aSwitch.setOnCheckedChangeListener(switchLed);
        return vista;
    }

    public void setHiloSalida(HiloSalida hiloSalida) {
        this.hiloSalida = hiloSalida;
    }

    private CompoundButton.OnCheckedChangeListener switchLed = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if(isChecked) {
                hiloSalida.enviarMensaje(Constante.SEÑAL_LUZ_HIGH);
            } else {
                hiloSalida.enviarMensaje(Constante.SEÑAL_LUZ_LOW);
            }
        }
    };

}
