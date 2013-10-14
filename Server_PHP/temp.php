<?php
	$startDate = 0;
	if (!isset($_GET['start']) ) 
		$startDate = 0;	
	else
		$startDate = intval($_GET['start']);
	
	$endDate = 0;
	
		if( isset($_GET['end']) ) {
			$endDate = intval($_GET['end']);	
		}
		
		$file = file_get_contents('./temp.txt');
		$rows = explode("\n",$file);
		array_pop($rows);
		
		if ( $startDate == 0 ) {
			echo end($rows), PHP_EOL;	
		}
		
		foreach($rows as $row => $data) {
			$dataList = explode(",",$data);
			$date = intval(array_shift($dataList));
			if ( $endDate == 0 ) {
				if ( $startDate == $date ) {
					echo $data, PHP_EOL;
					echo("<br>");
				}
			}
			else {
				if ( $date > $startDate && $date < $endDate  ) {
					echo $data, PHP_EOL;
					echo("<br>");
				}
			}
		}
?>
