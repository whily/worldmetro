#!/usr/bin/python
# -*- coding: utf-8 -*-
# Handle CSV file.

class CSV:
    def __init__(self, file):
        self.lines = file.readlines()
