import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Flutter Demo',
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.blue),
        useMaterial3: true,
      ),
      home: const MyHomePage(title: 'Justa / Smart POS'),
    );
  }
}

class MyHomePage extends StatefulWidget {
  const MyHomePage({super.key, required this.title});

  final String title;

  @override
  State<MyHomePage> createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  @override
  void initState() {
    super.initState();
    eventPlatform.receiveBroadcastStream().listen((event) {
      _processMethodChannelEvent(event);
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        backgroundColor: Theme.of(context).colorScheme.inversePrimary,
        title: Text(widget.title),
      ),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: <Widget>[
            const Text(
              'Escolha uma opção abaixo:',
            ),
            ElevatedButton(
              onPressed: _initSdk,
              child: const Text('Iniciar SDK'),
            ),
            ElevatedButton(
              onPressed: _initPayment,
              child: const Text('Iniciar Pagamento'),
            ),
            ElevatedButton(
              onPressed: _initPrint,
              child: const Text('Imprimir exemplo'),
            ),
          ],
        ),
      ),
    );
  }

  static const platform = MethodChannel('br.com.justa.smartpos/aditum');
  static const eventPlatform =
      EventChannel('br.com.justa.smartpos/aditum/events');

  void _processMethodChannelEvent(dynamic result) {
    print('Evento: $result');
    // Faça seu processamento aqui.
  }

  void _initSdk() async {
    try {
      final result = await platform.invokeMethod('init', {
        'merchantActivationCode': '746613962',
        'applicationToken': 'mk_hIRPWZ8IeUOmARXknh3ww',
      });
      print('Resultado: $result');
    } on PlatformException catch (e) {
      print("Falha: '${e.message}'.");
    }
  }

  void _initPayment() async {
    try {
      final result = await platform.invokeMethod('pay', {
        'amount': 1000, // R$ 10,00
        'installmentNumber': 1, // 2x
        'paymentType': 2, // Credit
        'installmentType': 1, // Merchant
        'operationType': 1, // Authorization
      });
      print('Resultado: $result');
    } on PlatformException catch (e) {
      print("Falha: '${e.message}'.");
    }
  }

  void _initPrint() async {
    try {
      final result = await platform.invokeMethod('print', {
        // Vamos imprimir uma imagem padrão quando não enviamos uma.
        // A imagem padrão é a logomarca do Plug Justa.
        // 'imageByteArray': ByteData(0).buffer.asUint8List(),
      });
      print('Resultado: $result');
    } on PlatformException catch (e) {
      print("Falha: '${e.message}'.");
    }
  }
}
