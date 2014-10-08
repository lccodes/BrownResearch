<?php

if($_POST['type'] == "clicks"){
	$file = $_POST['username'] . '.txt';
	chdir("..");
	chdir("respice");
	chdir($_POST['username']);
	$raw = $_POST['click'];
	$nospan = "<span ";
	$contents = explode($nospan,$raw);
	$ready = $contents[0] .",". date("Y-m-d-h:i:sa") . "||";
	file_put_contents($file,$ready ,FILE_APPEND);
}elseif($_POST['type'] == "views"){
	$file = "views.txt";
	chdir("..");
	chdir("respice");
	$info = file_get_contents($file);
	$username = $_POST['username'] . ",";
	$parts = explode($username, $info);
	if(count($parts) == 1){
		$alot = $info . $username . "1|" . date("Y-m-d-h:i:sa") .  "~" .  $_POST['page'] . "\n";
		file_put_contents($file, $alot);
	}else{
		 $intro = $parts[0];
		//echo $parts[1];
		$conc = substr($parts[1], strpos($parts[1], "\n")+1);
		//echo $conc;
		$data = explode("|",$parts[1]);
		//echo $data[1];
		$almost = explode("\n",$data[1]);
		//echo $almost[0];
		$newtotal = intval($data[0])+1;
		$newend = $almost[0] . "," . date("Y-m-d-h:i:sa") .  "~" . $_POST['page'] . "\n";
		//echo $newend;
		$thewhole = $intro . $username . strval($newtotal) . "|" . $newend . $conc;
		file_put_contents($file, $thewhole);
	}
}elseif($_POST['type'] == "happiness"){
	$username = $_POST['username'];
	chdir("..");
	chdir("respice");
	chdir($username);
	$file = $username . '-happy.txt';
	$contents = $_POST['score'] . "," . date("Y-m-d-h:i:sa") . "||";
	file_put_contents($file, $contents, FILE_APPEND);
}elseif($_POST['type'] == "survey"){
	$username = $_POST['username'];
	chdir("..");
	chdir("respice");
	chdir($username);
	$file = $username . '-survey.txt';
	$contents = $_POST['results'] . "," . date("Y-m-d") . "||";
	file_put_contents($file, $contents, FILE_APPEND);	
}

?>
