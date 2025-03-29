
import gui
from uuid import getnode as get_mac


class Mac:
    def __init__(self):
        self.mac_address = get_mac()

    def __encrypt__(self):
        return




if __name__ == '__main__':
    new_mac = Mac()
    print(new_mac.mac_address)
    import doctest
    doctest.testmod()