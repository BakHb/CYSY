<?php
	$HOST = 'localhost';
	$USERNAME = 'id1494119_cysy';
	$PASSWORD = 'cysy2017';
	$DB = 'id1494119_cysy';

	$con = mysqli_connect($HOST,$USERNAME,$PASSWORD,$DB);
	
	if(!$con){
		die("Error in connection" . mysqli_connect_error());	
	}
	else
	{
		echo "<br><h3> Connection Succes... </h3>";
	}
?>
