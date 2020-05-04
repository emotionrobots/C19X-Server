#!/bin/sh

sudo iptables -A PREROUTING -t nat -i eth0 -p tcp --dport 80 -j REDIRECT --to-port 8080
sudo iptables -A PREROUTING -t nat -i eth0 -p tcp --dport 443 -j REDIRECT --to-port 8443
sudo sh -c "iptables-save > /etc/iptables.rules"
sudo apt-get install  iptables-persistent
