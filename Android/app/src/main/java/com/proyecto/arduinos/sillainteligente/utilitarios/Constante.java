package com.proyecto.arduinos.sillainteligente.utilitarios;

public class Constante {

    /***  CONSTANTES DE MENSAJES QUE SE ENVIAN A ARDUINO  ***/
    public static final String SEÑAL_LUZ_LOW = "MLU0"+'\n';
    public static final String SEÑAL_LUZ_HIGH = "MLU1"+'\n';

    public static final String SEÑAL_COOLER_LOW = "MCO0"+'\n';
    public static final String SEÑAL_COOLER_HIGH = "MCO1"+'\n';

    public static final String SEÑAL_SOUND_LOW = "MSO0"+'\n';
    public static final String SEÑAL_SOUND_HIGH = "MSO1"+'\n';

    /***  CONSTANTES DE MENSAJES QUE SE RECIBEN DESDE ARDUINO  ***/
    public static final String SEÑAL_TEMP_ARD_G = "ATE1"+'\n';
    public static final String SEÑAL_TEMP_ARD_R = "ATE2"+'\n';
    public static final String SEÑAL_TEMP_ARD_B = "ATE3"+'\n';

    public static final String SEÑAL_US_G = "AUS1"+'\n';
    public static final String SEÑAL_US_B = "AUS2"+'\n';

    public static final String SEÑAL_LUZ_ARD_G = "ALU1"+'\n';
    public static final String SEÑAL_LUZ_ARD_R = "ALU2"+'\n';
    public static final String SEÑAL_LUZ_ARD_B = "ALU3"+'\n';

    public static final String SEÑAL_POT_ARD_G = "APU1"+'\n';
    public static final String SEÑAL_POT_ARD_R = "APU2"+'\n';
    public static final String SEÑAL_POT_ARD_B = "APU3"+'\n';

    public static final String COD_TEMP = "ATE";
    public static final String COD_US = "AUS";
    public static final String COD_LUZ = "ALU";
    public static final String COD_POT = "APU";

    /***  CONSTANTES DE MENSAJES AL HANDLER ***/
    public static final int CODIGO_MENSAJE_TEMPERATURA = 10;
    public static final int CODIGO_MENSAJE_POTENCIOMETRO = 11;
    public static final int CODIGO_MENSAJE_LUMINOSIDAD = 12;
    public static final int CODIGO_MENSAJE_DISTANCIA = 13;
}
