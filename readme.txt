
Test Client for testint the OL Soap API

usage: wstest
 -count <arg>         Number of constituents to create
 -eventid <arg>       Event Id to attach
 -h                   Display help message.
 -hostname <arg>      Host name running Orange Leap Soap Service
 -password <arg>      Password
 -port <arg>          Port number running Orange Leap Soap Service
 -servicename <arg>   Service name where OrangeLeap is running.
 -sitename <arg>      Site name
 -username <arg>      User name


Example on how to run against sandbox

java -jar
target/orangeleap-client-1.0-SNAPSHOT-jar-with-dependencies.jar -count
5 - eventid 3 -hostname go.orangeleap.com -username olapi@sandbox
-password <password> -sitename sandbox -servicename sandbox

