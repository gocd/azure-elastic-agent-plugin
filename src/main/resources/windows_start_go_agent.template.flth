try {
  start-service "Go Agent" -ea Stop;
} catch {
 write-host "Changing service to run as system user";
 sc.exe config "Go Agent" obj= "LocalSystem" password= "invalid" type= own type= interact;
 sleep -s 5;
 start-service "Go Agent";
}