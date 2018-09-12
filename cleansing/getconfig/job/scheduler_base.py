import re
import sys
import os
import logging
import numpy as np
import pandas as pd
import dataset as ds
from abc import ABCMeta, abstractmethod
from getconfig.util import Util
from getconfig.config import Config
from getconfig.merge_master import MergeMaster
from getconfig.inventory.info import InventoryInfo
from getconfig.inventory.loader import InventoryLoader
from getconfig.inventory.collector import InventoryCollector
from getconfig.inventory.table import InventoryTableSet
from getconfig.master_data.template.job_list import MasterDataJobList
from getconfig.master_data.template.ship_list import MasterDataShipList
from getconfig.master_data.template.mw_list import MasterDataSoftwareList
from getconfig.master_data.template.net_list import MasterDataNetworkList
from getconfig.ticket.template.ticket_ia_server import TicketIAServer
from getconfig.ticket.template.ticket_sparc import TicketSparc
from getconfig.ticket.template.ticket_power import TicketPower
from getconfig.ticket.template.ticket_storage import TicketStorage
# from getconfig.ticket.template.ticket_network import TicketNetwork
from getconfig.ticket.template.ticket_port_list import TicketPortList
from getconfig.ticket.template.ticket_relation import TicketRelation

class SchedulerBase(metaclass=ABCMeta):
    INVENTORY_DIR = 'build'
    module_name = '変換処理'

    def __init__(self, inventory_source = INVENTORY_DIR, **kwargs):
        self.inventory_source = inventory_source

    def load():
        '''データロード'''

    def transfer():
        '''データ変換'''

    def classify():
        '''データ分類'''

    def regist():
        '''データ登録'''

