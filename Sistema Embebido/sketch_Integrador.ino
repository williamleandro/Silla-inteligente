
int tiempo_inicial=0;
int tiempo_actual=0;


//PINES
//DIGITALES
//1
//2 TEMPERATURA
int pinDigitalTemp=2;
     
//3 TEMPERATURA Y HUMEDAD

#define DHTPIN 8

//4 PULSADOR
const int pulsadorPin = 4;

//5 ULTRASONIDO 
const int Echo = 5;

//6 ULTRASONIDO
const int Trigger = 6;   

//7

//8 LED
int pinLed = 11;

//9 BLUETOOTH
int blueRX=10;
//10BLUETOOTH
int blueTX=9;
//11 COOLER
//int pinCooler=11; ALEX: LO CAMBIE AL 3
//12 BUZZER
int pinBuzzer=12;
//13
//PWM


//ANALOGICOS
//0 SENSOR LUZ
 
//3
//4
//5

//Variables para el cooler y potenciometro
word salidaPwm = 3;
int potenciometro=A1;
int valor = 0;
int valorPwm = 0;


// SENSOR TEMPERATURA Y HUMEDAD
// PIN NUMERO 2
// LIMITE TEMPERATURA 
// LIMITE Humedad

#include <DHT.h>
#define humedad_maxima 100
#define temperatura_maxima 30

#define DHTTYPE DHT22   // Sensor DHT22
DHT dht(DHTPIN, DHTTYPE);
// FIN SENSOR TEMPERATURA Y HUMEDAD
//


//SENSOR DE LUZ
//PIN DIGITAL NUMERO 8
//PIN ANALOGICO NUMERO 0
//LIMITE DE LUZ 45

int ledPinAnalog = 0;//Pin analógico
int val01 = 0;//Valor del sensor de luz
int min_light=45;//Intervalo de luz. Ajustar y cambiarlo por el de nuestro entorno

//FIN SENSOR LUZ


// SENSOR DE TEMPERATURA
// PIN DIGITAL NUMERO 2

#include <DallasTemperature.h>
#include <OneWire.h>        


OneWire ourWire(pinDigitalTemp);
DallasTemperature sensors(&ourWire); //Se declara una variable u objeto para nuestro sensor

//FIN SENSOR TEMPERATURA

//PULSADOR
//PIN DIGITAL 2
//FIN PULSADOR

//BLUETOOTH
#include <SoftwareSerial.h>   // Incluimos la librería  SoftwareSerial  
SoftwareSerial BT(blueRX,blueTX);    // Definimos los pines RX y TX del Arduino conectados al Bluetooth
char character;
String data = "";

bool prendido=true;
long valorAnterior=0;
int distanciaMinima=12; //CM de distancia para el ultrasonido


int valorSonido=0;
int valorSonidoTemperatura=0;
int valorSonidoPulsador=0;
int valorLuz=0;
int valorLuzPulsador=0;
int valorLuzTemperatura=0;
int ultrasonido=0;
int temperatura=0;
int humedad=0;
int luminosidad=0;
int pulsadorInt = 0;
int pulsador=0;
int contadorPulsador=5;
int estadoLed=0;
bool encendido=false;
bool estado1=false;
bool estado2=false;
bool estado3=false;
bool estado4=false;
int lecturaPotAnterior=0;
bool nuncaSono=true;

void setup() {
  // put your setup code here, to run once:

  pinMode(salidaPwm, OUTPUT);
  pinMode(potenciometro, INPUT);
  configura25KhzPwm();//cosa rara del cooler

  Serial.begin(9600);
  tiempo_inicial = millis();
  
  // INICIALIZO EL SENSOR DE TEMPERATURA Y HUMEDAD
  Serial.println("Iniciando sensor temp y humedad DHT22 ...");
  dht.begin();
  // FIN EL SENSOR DE TEMPERATURA Y HUMEDAD

  // INICIALIZO EL SENSOR DE LUZ
  //Serial.println("Iniciando sensor temp y humedad DHT22 ...");
  //FIN SENSOR DE LUZ

  //INICIALIZO SENSOR DE TEMPERATURA
  sensors.begin();   //Se inicia el sensor
  //FIN SENSOR TEMPERATURA

  //INICIO PULSADOR
    pinMode(pulsadorPin, INPUT);
  //FIN PULSADOR
  
  //PIN DIGITAL 
  pinMode(pinLed, OUTPUT);
  pinMode(pinBuzzer,OUTPUT);
  //FIN LUCES LED
  
  pinMode(Trigger, OUTPUT); //pin como salida
  pinMode(Echo, INPUT);  //pin como entrada
  pinMode(13,OUTPUT);
  digitalWrite(Trigger, LOW);//Inicializamos el pin con 0


  BT.begin(9600);  
  
} 

void loop() {
  // put your main code here, to run repeatedly:
  tiempo_actual = millis()-tiempo_inicial;
  int valor;
  if(tiempo_actual > 1000)
  {
    tiempo_inicial=millis();
    if(encendido==true)
    {
      if(nuncaSono==true)
      {
        //PrimerEncendido();
      }
      BlueRead();
      SensorTempHum();
      SensorLuz();
      SensorTemperatura();
      Pulsador();
      //valor=Potenciometro();
      //Luz(valor);  //comentado para pruebas de on/off led
      UltraSonido();
      Cooler();
      BlueTooth();
  
      

    }
    
    else
    {
      encendido=Encender();
    }
  }
  else
  {
    
  }
}


void PrimerEncendido()
{
  Sonido(true);
  delay(500);
  Sonido(false);
  delay(500);
  Sonido(true);
  delay(2000);
  Sonido(false);
  delay(700);
  Sonido(true);
  delay(200);
  Sonido(false);
  nuncaSono=false;
  Luz(false);
}

void Cooler(){
    valor=analogRead(potenciometro);
    valorPwm=valor/4;
    //Serial.println("\nvalor del pwm");
    //Serial.println(valorPwm);
    analogWrite(salidaPwm,valorPwm);
  }


bool Encender()
{
  
  int lectura = Potenciometro();
  
  if(!estado1)
  {
    lecturaPotAnterior=lectura;
    if(lectura>100)
    {
      return false;
    }
    else
    {
      estado1=true;
      return false;
    }
  }

  if(!estado2)
  {
    if(lectura<300 || lectura > 600)
    {
      return false;
    }
    else
    {
      Luz(true);
      estado2=true;
      return false;
    }
  }


  if(!estado3)
  {
    Luz(false);
    if(lectura<900)
    {
        return false;
    }
    else
    {
      estado3=true;
      return false;    
    }
  }

  if(!estado4)
 {
    if(lectura<300 || lectura > 600)
    {
      return false;
    }
    else
    {
      Luz(true);
      estado4=true;
      return false;
    }
 }

 return true;
}


int Potenciometro()
{
  
  int valor = analogRead(potenciometro);
  Serial.println(valor);
  return valor;
}

void SensorTempHum()
{
  float h = dht.readHumidity(); //Leemos la Humedad
  float t = dht.readTemperature(); //Leemos la temperatura en grados Celsius

  //Serial.println("Datos Sensor DHT22");
  //Serial.print("Humedad ");
  //Serial.print(h);
  //Serial.print(" %t");
  //Serial.print("Temperatura: ");
  //Serial.print(t);
  //Serial.print(" *C ");
  //Serial.print("\n");

  if ( h < humedad_maxima)
  {
    humedad=0;
      HumedadOptima();
  }
  else
  {
    humedad=1;
      HumedadExedida();
  }

  if ( t < temperatura_maxima)
  {
      TemperaturaOptima();
  }
  else
  {
      TemperaturaExedida();
  }
  
}

void HumedadExedida()
{
  
}

void TemperaturaExedida()
{
  //Serial.println("Temperatura exedida");
  valorSonidoTemperatura=1;
  valorLuzTemperatura=1;
  //EncenderLuz(pinLed);
}

void TemperaturaOptima()
{
  ApagarSonido(pinBuzzer);
  valorSonidoTemperatura=0;
  valorLuzTemperatura=0;
  //ApagarLuz(pinLed);
}

void HumedadOptima()
{
  
}

void SensorLuz()
{
  Serial.println("LUZ");
  val01 = analogRead(ledPinAnalog);
  
  if (val01>=min_light)
  {
    luminosidad=0;
    Serial.println(val01);
    //Luz(false);//ALEX AGREGO ESTO
    ////Serial.println("LUZ ENCENDIDA");
    //Serial.println("LED APAGADO");
  }
  else
  {
    luminosidad=1;
    Serial.println(val01);
    Luz(true);
    //Serial.println("LUZ APAGADA");
    //Serial.println("LED ENCENDIDO");
  }
}



void SensorTemperatura() {
  sensors.requestTemperatures();   //Se envía el comando para leer la temperatura
  float temp= sensors.getTempCByIndex(0); //Se obtiene la temperatura en ºC

  //Serial.print("Temperatura= ");
  //Serial.print(temp);
  //Serial.println(" C");

  if(temp > temperatura_maxima)
  {
      temperatura=1;
  }
  else
  {
      temperatura=0;
  }
  
}


bool Pulsador()
{
  pulsadorInt = digitalRead(pulsadorPin);
  //Serial.print(pulsador);
  if (pulsadorInt == LOW) 
  {
      contadorPulsador=5;
      pulsador=LOW;
      valorSonidoPulsador =1;
      valorLuzPulsador=1;
      return true;
  }
  else 
  {
    contadorPulsador--;
    if(contadorPulsador==0)
    {
      pulsador=HIGH;
    }
    valorSonidoPulsador=0;
    valorLuzPulsador=0;
    return false;
  }
}


void EncenderLuz(int pin)
{
  digitalWrite(pin,HIGH);  
}

void ApagarLuz(int pin)
{
  digitalWrite(pin,LOW);  
}

void EncenderSonido(int pin)
{
  valorSonido=1;
  digitalWrite(pin,HIGH);  
}

void ApagarSonido(int pin)
{
  valorSonido=0;
  digitalWrite(pin,LOW);  
}

void Sonido(bool prender)
{
  if(prender==true)
  {
    digitalWrite(pinBuzzer,HIGH);  
  }
  else
  {
    digitalWrite(pinBuzzer,LOW);  
  }
}



void Luz(int valor)
{
  valor=valor/4;

  if(valor > 40)
  {
    estadoLed=1;
    analogWrite(pinLed,valor);
  }
  else
  {
    estadoLed=0;
    analogWrite(pinLed,0);
  
  }
}

void Luz(bool prendido)
{
  if(prendido==true)
  {
    Luz(700);
  }
  else
  {
    Luz(0);
  }
}


void UltraSonido()
{

  unsigned long t; //timepo que demora en llegar el eco
  long d; //distancia en centimetros
  

  digitalWrite(Trigger, HIGH);
  delayMicroseconds(10);          //Enviamos un pulso de 10us
  digitalWrite(Trigger, LOW);
  
  t = pulseIn(Echo, HIGH); //obtenemos el ancho del pulso
  d = ( (t/2) / 29.1 );             //escalamos el tiempo a una distancia en cm


  //if( ( (d < valorAnterior - 5) || (d > valorAnterior + 5 )   ) )
  if( d < distanciaMinima)
  {
    prendido=true; 
    //valorAnterior=d; 
  }

  if(prendido)
  {
      ultrasonido=1;
  }
  else
  {
      ultrasonido=0;
  }
  prendido=false;  
  
  Serial.println("Distancia: ");
  Serial.println(d);      //Enviamos serialmente el valor de la distancia
  Serial.println();
  //Serial.print("cm");
  //Serial.println();
}


void BlueRead()
{
  while(BT.available())    // Si llega un dato por el puerto BT se envía al monitor serial
  {
    character = BT.read();
    data.concat(character);
    if (character == '\n')
    {
          Serial.println(data);

          if(data=="MSO1\n")
          {
            Sonido(true);
          }

          if(data=="MSO0\n")
          {
            Sonido(false);
          }
          
          if(data=="MLU1\n")
          {

            Luz(true);
          }

          if(data=="MLU0\n")
          {

            Luz(false);
          }
          
          
          if(data=="MTS1\n")
          {
            Sonido(true);
          }

          if(data=="MCO1\n")
          {
            Sonido(true);
          }
          
          if(data=="MCO0\n")
          {
            Sonido(false);
          }

          char cadena[10];
          data.toCharArray(cadena,5);
          if(cadena[0]=='P' && cadena[1]=='W')
          {
              char inChara,inCharb;
              inChara=cadena[2];
              inCharb=cadena[3];
              int potencia;
              String inString="";
              if (isDigit(inChara) && isDigit(inCharb)) 
              {
              // convert the incoming byte to a char and add it to the string:
                inString += (char)inChara;
                inString += (char)inCharb;
                potencia = inString.toInt();
                Serial.println("Valor potencio recibido");
                Serial.println(potencia);
                Luz(potencia*10); //Multiplico por diez ya que el valor recibido es entre 0 y 99 y me interesa enviar un valor entre 0 y 1000
              } 
          }
          
          data = "";
    }
    //Serial.write(input);
  }  
}

void BlueWrite(String mensaje)
{
  //mensaje+="\n";
  char cadena[10];
  mensaje.toCharArray(cadena,5);
  //Serial.println(cadena);
  BT.write(cadena);
}

void BlueTooth()
{
  BT.write("#");
  
  if(ultrasonido==1)
  {
    BlueWrite("AUS1");
  }
  else
  {
    BlueWrite("AUS2");
  }

  if(temperatura==1)
  {
    BlueWrite("ATE3");
  }
  else
  {
    BlueWrite("ATE1");
  }


  if(luminosidad==1)
  {
    BlueWrite("ALU3");
  }
  else
  {
    BlueWrite("ALU1");
  }

  if(humedad==1)
  {
    BlueWrite("ATH3");
  }
  else
  {
    BlueWrite("ATH1");
  }

  if(pulsador==LOW)
  {
    BlueWrite("APU1");
  }
  else
  {
    BlueWrite("APU0");
  }


  if(estadoLed==1)
  {
   BlueWrite("ALE1");
  }
  else
  {
    BlueWrite("ALE0");
  }

  
  BT.write("@");
}





void configura25KhzPwm() {
  TCCR2A = 0;                                               // TC2 Control Register A
  TCCR2B = 0;                                               // TC2 Control Register B
  TIMSK2 = 0;                                               // TC2 Interrupt Mask Register
  TIFR2 = 0;                                                // TC2 Interrupt Flag Register
  TCCR2A |= (1 << COM2B1) | (1 << WGM21) | (1 << WGM20);    // OC2B cleared/set on match when up/down counting, fast PWM
  TCCR2B |= (1 << WGM22) | (1 << CS21);                     // prescaler 8
  OCR2A = 79;                                               // TOP overflow value (Hz)
  OCR2B = 0;
}

//void cicloPwm(byte ocrb) {
//  OCR2B = ocrb;                                            // PWM Width (duty)
//}
