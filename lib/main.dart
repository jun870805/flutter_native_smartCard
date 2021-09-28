import 'dart:async';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'dart:io';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      title: 'InfoThink IT100U SmartCard',
      theme: ThemeData(
        primarySwatch: Colors.blue,
      ),
      home: const MyHomePage(title: 'InfoThink IT100U SmartCard'),
    );
  }
}

class MyHomePage extends StatefulWidget {
  const MyHomePage({Key? key, required this.title}) : super(key: key);

  final String title;

  @override
  State<MyHomePage> createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {

  static const EventChannel _channel = EventChannel('flutter_and_native');

  late dynamic _streamSubscription;
  late String _platformMessage = "no message";

  void _enableEventReceiver() {
    _streamSubscription = _channel.receiveBroadcastStream().listen((dynamic event) {
      debugPrint('Received event: $event');
      String changeMessage = "";
      if(event['message'].toString()!="null"){
        changeMessage = event['message'].toString();
      }
      setState(() {
        _platformMessage = changeMessage;
      });
    },onError: (dynamic error) {
      debugPrint('Received error: ${error.message}');
    }, cancelOnError: true);
  }

  void _disableEventReceiver() {
    if (_streamSubscription != null) {
      _streamSubscription.cancel();
      _streamSubscription = null;
    }
  }

  @override
  void initState() {
    super.initState();
    _enableEventReceiver();
  }

  @override
  void dispose() {
    super.dispose();
    _disableEventReceiver();
  }


  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(widget.title),
      ),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: <Widget>[
            const Text(
              'You have receive message:',
            ),
            Text(
              _platformMessage,
              style: Theme.of(context).textTheme.headline4,
            ),
          ],
        ),
      ),
    );
  }
}
