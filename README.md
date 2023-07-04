# MQTT-Broker

__What is a MQTT Broker?__

A MQTT (Message Queuing Telemetry Transport) broker is a software component that acts as a central hub or intermediary for the communication between MQTT clients. MQTT is a lightweight messaging protocol designed for efficient and reliable communication between devices in IoT (Internet of Things) and other resource-constrained environments.
The MQTT broker receives messages published by MQTT clients and delivers them to the intended recipients. It enables the decoupling of senders (publishers) and receivers (subscribers) of messages, allowing devices to communicate with each other without direct knowledge of one another.
Clients connect to the MQTT broker using the MQTT protocol, establish a session, and subscribe to specific topics or publish messages to topics of interest. The broker receives published messages and forwards them to the subscribed clients based on the topic subscriptions.

#
This was a model built in Java with Scene builder, the interface is very simple and i think it doesn't need an explanation, you can test it using a free and public Broker, like the one i used in the example.

![MQTT Broker 3](https://github.com/RicardoFamiliar/MQTT-Broker/assets/117604174/b38f0c73-1867-4543-9d05-d0fce527b4dc)

You can see the code or run the MQTT.jar that is listed above to test the software.
