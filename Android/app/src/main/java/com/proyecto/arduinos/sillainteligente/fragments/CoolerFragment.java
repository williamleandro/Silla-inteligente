package com.proyecto.arduinos.sillainteligente.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.proyecto.arduinos.sillainteligente.R;
import com.proyecto.arduinos.sillainteligente.utilitarios.Constante;
import com.proyecto.arduinos.sillainteligente.hilos.HiloSalida;

/**
 * A simple {@link Fragment} subclass.
 */
public class CoolerFragment extends Fragment {
    private BootstrapButton btnCooler;
    private HiloSalida hiloSalida;

    public CoolerFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View vista = inflater.inflate(R.layout.fragment_cooler, container, false);

        this.btnCooler = vista.findViewById(R.id.button);
        this.btnCooler.setOnClickListener(btnAccionCooler);

        return vista;
    }

    public void setHiloSalida(HiloSalida hiloSalida) {
        this.hiloSalida = hiloSalida;
    }

    private View.OnClickListener btnAccionCooler = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            hiloSalida.enviarMensaje(Constante.SEÑAL_COOLER_HIGH);
        }
    };

}
