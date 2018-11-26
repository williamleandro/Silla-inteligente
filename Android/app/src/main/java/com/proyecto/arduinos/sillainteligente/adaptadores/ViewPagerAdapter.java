package com.proyecto.arduinos.sillainteligente.adaptadores;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.proyecto.arduinos.sillainteligente.fragments.CoolerFragment;
import com.proyecto.arduinos.sillainteligente.fragments.LEDFragment;
import com.proyecto.arduinos.sillainteligente.hilos.HiloSalida;

import java.util.ArrayList;
import java.util.List;

public class ViewPagerAdapter extends FragmentPagerAdapter {
    private List<Fragment> listaFragmento = new ArrayList<>();
    private List<String> tituloFragmento = new ArrayList<>();

    public ViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        return this.listaFragmento.get(i);
    }

    @Override
    public int getCount() {
        return this.listaFragmento.size();
    }

    public void addFragment(Fragment fragment, String titulo) {
        this.listaFragmento.add(fragment);
        this.tituloFragmento.add(titulo);
    }

    public void setThreadFragment(HiloSalida hiloSalida) {
        for(int i=0; i<listaFragmento.size(); i++) {
            String titulo = tituloFragmento.get(i);

            if(titulo.equals(new String("LED"))) {
                LEDFragment led = (LEDFragment) listaFragmento.get(i);
                led.setHiloSalida(hiloSalida);
            }

            if (titulo.equals(new String("Cooler"))) {
                CoolerFragment cooler = (CoolerFragment) listaFragmento.get(i);
                cooler.setHiloSalida(hiloSalida);
            }
        }
    }

    public void setSenialLuminosidad(boolean flag) {
        for(int i=0; i<listaFragmento.size(); i++) {
            String titulo = tituloFragmento.get(i);

            if(titulo.equals(new String("LED"))) {
                LEDFragment led = (LEDFragment) listaFragmento.get(i);
                led.setFlagLuzEncendida(flag);
            }
        }
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return this.tituloFragmento.get(position);
    }
}
