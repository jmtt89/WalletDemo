# WalletDemo
Application that receives remote passes and displays them natively through the Messangi Platform

<div float="left">
  <img src="https://raw.githubusercontent.com/jmtt89/WalletDemo/master/Notification.png" width="24%"/>
  <img src="https://raw.githubusercontent.com/jmtt89/WalletDemo/master/mainList.png" width="24%"/>
  <img src="https://raw.githubusercontent.com/jmtt89/WalletDemo/master/boardingPass.png" width="24%"/>
  <img src="https://raw.githubusercontent.com/jmtt89/WalletDemo/master/coupon.png" width="24%"/>
</div>

# WalletSDK

El Wallet SDK es una librería enfocada agregar capacidades propias de aplicaciones wallet. **La librería se distribuye mediante JCenter**, por lo que su inclusión es muy sencilla de realizar.  

Para simplificar la integracion con aplicaciones, se proveen dos clases principales `WalletManager` y `WalletApp`.

- **WalletApp**: Es la clase principal de configuración de la librería, permite asignar el *deviceId* y el *pushToken* para su identificación en el proceso de suscripción.  

- **WalletManager**: Es la clase que permite la interacción principal entre la aplicacion y la libreria. Mediante esta clase permitimos *Agregar*, *Listar*, *Mostrar*, *Eliminar* y *Subscribir* pases. 

## Dependencia
Para incluir esta librería es necesario agregar la siguiente dependencia en la sección de dependencias en su archivo **Gradle a nivel de aplicación**.  

```JSON
...  
android {  
    ...  
}  

dependencies {  
	...
	implementation 'com.ogangi.oneworldwallets.wallet:walletsdk:1.0.1'  
	...
}  
...
```

## Configuración
Si desea personalizar datos como el nombre de su aplicacion o el identificador de su dispositivo, puede hacerlo mediante la clase **WalletApp** esta es la clase principal de configuración.

Esta clase debe ser inicializada en el método onCreate de su clase Application, esta clase es ejecutada al iniciar su aplicacion antes que cualquier activity, para más informacion de la clase Application puede consultar la [documentacion oficial](https://developer.android.com/reference/android/app/Application). La Clase Application debe ser referenciada su manifest en el atributo *android:name*. 

```xml
//AndroidManifest.xml
<manifest  ...>
 	<application android:name=".MyApp" ...>
		...
	</application>
</manifest>
```


```JAVA
//MyApp.java
public class MyApp extends Application {
	@Override  
	public void onCreate() {  
	    super.onCreate();
	    WalletApp.initialize(getApplicationContext());
    }
}
``` 

### Device Id (Opcional)

Para asignar un identificador de dispositivo puede utilizar el método **setDeviceId**. Si no lo hace la librería asignará automáticamente un identificador único a este dispositivo. 

```JAVA
	String uniqueDeviceId = ...
	WalletApp.getInstance().setDeviceId(uniqueDeviceId);
``` 

> **NOTA** si no posee un método seguro para la obtención del  identificador único por dispositivo es preferible no modificar este campo ya que puede producir inconsistencias en el registro y procesamiento de sus pases.   

## Almacenar Pases
La librería permite almacenar pases de manera **offline** en el dispositivo. Estos pases se encuentran disponibles para su uso posterior una vez agregados

Para agregar un pase es necesario tener un archivo pkpass físico en el dispositivo o un url que apunte a uno. 

El método utilizado para agregar un pase es el siguiente:
```Java
	Uri uri = Uri.parse("https://...");
	WalletManager.getInstance().addPass(uri);
```
  

## Mostrar Pases
El Renderizado de los pases es 100% Nativo de Android utilizando tecnologías propias de la plataforma lo que garantiza mayor fluidez y personalización que soluciones híbridas basadas en webviews. 

La librería provee el Fragment **PassContainerFragment** que es la forma más cómoda de agregar tu pase a cualquier interfaz desarrollada en Android de manera cómoda. 

La forma más sencilla de utilizar este fragmento es mediante la clase FragmentManager

```JAVA
PassWallet pass = ....;
String passId = pass.getId();
getFragmentManager()
  .beginTransaction()
  .replace(R.id.fragment, PassContainerFragment.newInstance(passId))
  .commit();
```

La forma de utilizar el fragment es exactamente igual a cualquier otro fragment desarrollado en Android. El PassId es el identificador del pase, la forma más sencilla de obtenerla es utilizando el método **getId** de la clase  **PassWallet**

#### Callback del Fragment (Opcional)
La actividad que incluye el fragment puede implementar de manera opcional la interfaz **OnPassInteractionListener**. Esta interfaz permite comunicarse desde el fragmento hacia la actividad que lo contiene cuando ocurre alguna interacción en el pase.  

```JAVA
public class PassActivity extends AppCompatActivity implements OnPassInteractionListener{
	...
}
```

## Listar Pases
Para listar todos los pases almacenados en el dispositivo, puede utilizar el método **getPasses** de la clase **WalletManager**.

```JAVA
List<PassWallet> passes = WalletManager.getInstance().getPasses();
```

Si quieres obtener un único pase a partir de un passId puede utilizar el método:

```JAVA
PassWallet pass = WalletManager.getInstance().getPasse(<String> PassId);
```

## Eliminar Pases
Para eliminar los pases del dispositivo puede utilizar alguno de los siguientes métodos:

- Eliminar un pase mediante Id
```JAVA
	String passId = ...;
	WalletController.getInstance().deletePass(passId);
```
- Eliminar un pase mediante el pase
```JAVA
	PassWallet pass = ...;
	WalletController.getInstance().deletePass(pass);
```
- Eliminar todos los pases del dispositivo.
```JAVA
	WalletController.getInstance().deletePasses(passId);
```

## Actualizar Pases
La librería permite actualizar un pase previamente agregado o un conjunto de pases agrupados por *passTypeIdentifier*. 

La forma de actualizar el pase es descargando una nueva copia del mismo, de este proceso de carga y descarga se encarga al completo el SDK y la aplicacion no tiene que intervenir. 

### Actualizar un Pase, utilizando el propio pase
Si desea obtener la última versión de un pase en particular, puede utilizar el método **updatePass** de la clase **WalletManager**. 

```JAVA
	PassWallet pass = ...;
	WalletManager.getInstance().updatePass(pass);
```

### Actualizar pases mediante PassTypeIdentifier y SerialNumber
Si desea actualizar todos los pases que compartan *PassTypeIdentifier* puede utilizar el metodo **updatePasses** de las clase **WalletManager**. Este método recibe como parámetro dos String, *PassTypeIdentifier* y *SerialNumber*, segun la documentacion oficial de Apple estos dos campos son los que identifican un pase. Mediante este método puede

- Actualizar un pase en especifico
```JAVA
	String passTypeIdentifier = "...";
	String serialNumber = "...";
	WalletManager.getInstance().updatePasses(passTypeIdentifier, serialNumber);
```

- Actualizar un conjunto de pases (Pasando *SerialNumber* como *null*)
```JAVA
	String passTypeIdentifier = "...";
	WalletManager.getInstance().updatePasses(passTypeIdentifier, null);
```

- Actualizar todos los pases (Pasando ambos parámetros como null)
```JAVA
	WalletManager.getInstance().updatePasses(null, null);
```
> Este método suele ser el indicado para el caso de actualizar el pases mediante notificaciones push de manera transparente para el usuario.


<!-- Aun no soportamos esto
## Suscripciones
Los pases permiten suscribirse y desuscribirse del servicio de notificaciones push para actualizaciones. 

La librería **no maneja la recepción de las notificaciones push**, la aplicacion es quien recibe dichas notificaciones y en base a estas, debe actualizar el pase correspondiente. El motivo para no manejar estas notificaciones es principalmente mantenerse independiente de soluciones particulares en la recepción de estas notificaciones, pudiendo ser asi mas facil portar a múltiples aplicaciones de terceros.

### Configuración de PUSH
Para configurar la posibilidad de recibir actualizar los pases mediante notificaciones push, es necesario asignar el **pushToken** a la librería.

```Java
	String pushToken = ...
	WalletApp.getInstance().setPushToken(pushToken);
```
La librería se encarga de suscribir y desuscribir automáticamente las notificaciones push al agregar o eliminar un pase mediante el método respectivo, siempre que el *pushToken* esté disponible. Así mismo si el *pushToken* o el *deviceId* es actualizado, todos los pases almacenados se sincronizan automáticamente con estos nuevos elementos, sustituyendo los previos.   

### Suscribir
Suscribe un pase para recibir notificaciones push cuando ocurra una actualización del mismo.

```JAVA
	PassWallet pass = ...;
	WalletManager.getInstance().subscribe(pass);
```

> **NO** es necesario utilizar este método luego de los métodos **addPass**, **setPushToken** o **setDeviceId** ya que este se utiliza automáticamente de estar *pushToken* disponible. 

### Desuscribir
Elimina una suscripción para dejar de recibir notificaciones en el device cuando ocurra una actualización del mismo. 

```JAVA
	PassWallet pass = ...;
	WalletManager.getInstance().unsubscribe(pass);
```

>**NO** es necesario utilizar este método luego de **Eliminar Pases** ya que este se utiliza automáticamente de estar *pushToken* disponible. 

### Checkear Suscripcion
Para verificar si un pase se encuentra suscrito puede utilizar el método **isSubscribe** de la clase **WalletManager**.

```JAVA
	PassWallet pass = ...;
	boolean isSubscribe = WalletManager.getInstance().isSubscribe(pass);
```

Para verificar si la librería puede Suscribir pases para recibir notificaciones push puede utilizar el método **canSubscribe**.

```JAVA
boolean canSubscribe = WalletManager.getInstance().canSubscribe();
```
-->
