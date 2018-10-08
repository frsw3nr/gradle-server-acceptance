class InventoryInfo(object):

    def __init__(self, path, name, project, timestamp, **kwargs):
        self.source    = path
        self.name      = name
        self.project   = project
        self.timestamp = timestamp
