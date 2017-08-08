import urllib2
import time
import json
from kafka import KafkaProducer


def main():

	# kafka v2.11-0.11.0.0
	producer = KafkaProducer(bootstrap_servers='localhost:9092',
	                         client_id='ian_local',
	                         value_serializer=str.encode,
	                         # value_serializer=lambda v: json.dumps(v).encode('utf-8'),
	                         key_serializer=str.encode)

	price_url = 'http://www.ercot.com/content/cdr/contours/rtmLmpHgPoints.kml'
	
	while True:
		message = ""
		in_message = 0
		count = 1
		for line in urllib2.urlopen(price_url):

			for char in line:
				print char

			if '<Placemark>' in line:
				in_message = 1

			if in_message == 1:
				message = message + line

			if '</Placemark>' in line:

				# send message
				print '- - - - - - - NEW MESSAGE - - - - - - -'
				print message
				producer.send('price', key='price', value=message)
				print '- - - - - - - MESSAGE COUNT: {}- - - - - - -'.format(count)

				# reset message
				message = ""
				in_message = 0
				count += 1

				# emulate a streaming datasource
				# approx 250-300 nodal prices to report from each batch
				# check the data URL for fresh data every 4-5 minutes (but broadcast data whether it's new or not)
				time.sleep(1)


if __name__ == '__main__':
    main()
