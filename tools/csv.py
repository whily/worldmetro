#!/usr/bin/python
# -*- coding: utf-8 -*-
# Handle CSV file according to http://tools.ietf.org/html/rfc4180

class CSV:
    def __init__(self, file):
        lines = [line.decode("utf-8").split(",") for line in file.readlines()]
        self.header = lines[0]
        self.records =  lines[1:]
