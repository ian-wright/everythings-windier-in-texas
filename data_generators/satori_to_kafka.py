import sys
import time
import json
from kafka import KafkaProducer
from satori.rtm.client import make_client, SubscriptionMode

endpoint = "wss://open-data.api.satori.com"
appkey = "586dB9E0ED4A87af28AeBb268Be1B429"
channel = "METAR-AWC-US"

# use this to tune the volume of streaming weather data
send_fraction = 0.5

# kafka v2.11-0.11.0.0
producer = KafkaProducer(bootstrap_servers='localhost:9092',
                         client_id='ian_local',
                         value_serializer=lambda v: json.dumps(v).encode('utf-8'),
                         key_serializer=str.encode)

def main():
    with make_client(endpoint=endpoint, appkey=appkey) as client:
        print('Connected to Satori RTM!')

        class SubscriptionObserver(object):
            # Called when the subscription is established.
            def on_enter_subscribed(self):
                print "Subscribed to the channel '{}' with send_fraction {}".format(channel, send_fraction)

            def on_enter_failed(self, reason):
                print 'Subscription failed, reason:', reason
                sys.exit(1)
            def on_subscription_data(self, data):
                count = 0
                for i, message in enumerate(data['messages']):
                    # send message to kafka
                    if i % (1 / send_fraction) == 0:
                        producer.send('weather', key='weather', value=message)
                        count += 1
                print 'sending {} msgs in batch'.format(count)

        subscription_observer = SubscriptionObserver()
        client.subscribe(
            channel,
            SubscriptionMode.SIMPLE,
            subscription_observer)

        try:
            while True:
                time.sleep(1)
                pass
        except KeyboardInterrupt:
            pass


if __name__ == '__main__':
    main()


# SATORI MSG EXAMPLE
# {u'wind_speed_kt': u'3', 
# u'elevation_m': u'8.0', 
# u'quality_control_flags': u'\n        TRUE\n        TRUE\n      ',
# u'observation_time': u'2017-07-16T01:30:00Z',
# u'sky_condition': u'',
# u'temp_c': u'28.0',
# u'visibility_statute_mi': u'10.0',
# u'metar_type': u'SPECI',
# u'wind_dir_degrees': u'140',
# u'flight_category': u'VFR',
# u'longitude': u'-77.4',
# u'dewpoint_c': u'23.0',
# u'latitude': u'35.62',
# u'raw_text': u'KPGV 160130Z AUTO 14003KT 10SM BKN050 BKN060 28/23 A2996 RMK AO2',
# u'station_id': u'KPGV',
# u'altim_in_hg': u'29.958662'}



