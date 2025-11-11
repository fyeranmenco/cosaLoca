
mvn clean install compile;
@("api-gateway", "servicio-clientes", "servicio-depositos", "servicio-camiones", "servicio-integracionGM", "servicio-solicitudes", "servicio-usuarios").forEach(
	{Start-Process -FilePath "powershell.exe" -ArgumentList "-NoExit", "-Command", "[console]::Title = '$PSItem';", "cd $PSItem ; mvn spring-boot:run"})