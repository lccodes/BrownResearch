#!/usr/bin/python

import socket

sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
sock.connect(('127.0.0.1', 1300))

while True:
    message = sock.recv(512).rstrip()
    if not message:
        break
    print message
