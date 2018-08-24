import re
import sys
import os
import logging
import numpy as np
import pandas as pd

class InventoryData(object):

    def __init__(self, host_list = pd.DataFrame(), port_list = pd.DataFrame(), **kwargs):
        self.host_list = host_list
        self.port_list = port_list

    def print(self, columns = None):
        if columns:
            print("HOSTS : ", self.host_list[columns])
        else:
            print("HOSTS : ", self.host_list)
        print("PORTS : ", self.port_list)

    def count(self):
        return len(self.host_list)

    def port_count(self):
        return len(self.port_list)
