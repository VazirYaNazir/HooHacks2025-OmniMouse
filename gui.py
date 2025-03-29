
import sys
from PyQt6.QtCore import QSize, Qt
from PyQt6.QtGui import QIcon, QFont
from PyQt6.QtWidgets import QApplication, QMainWindow, QPushButton, QVBoxLayout, QWidget, QHBoxLayout, QLabel, \
    QBoxLayout
import os


app = QApplication(sys.argv)

class MainWindow(QMainWindow):
    def __init__(self):
        super().__init__()
        self._main_window_atr_()
        self._main_button_atr_()
        self._side_bar_atr_()


    def _main_window_atr_(self):
        """Configure the main window, central widget, and main layout."""
        self.setWindowTitle("Omni Mouse")
        self.resize(1980, 1080)

        # Create a central widget and set it for QMainWindow
        central_widget = QWidget()
        self.setCentralWidget(central_widget)

        # Create a horizontal layout for the entire window (sidebar + main content)
        self.main_layout = QHBoxLayout(central_widget)

    def _main_button_atr_(self):
        """
        Create a main content area with a primary button or label.
        """
        # Create a container for main content on the right side
        self.content_widget = QWidget()
        self.content_layout = QVBoxLayout(self.content_widget)

        # Example label
        self.main_label = QLabel("Gyroscopic Information goes here")
        self.main_label.setAlignment(Qt.AlignmentFlag.AlignCenter)
        self.content_layout.addWidget(self.main_label)

        self.main_button = QPushButton("Get Mac Address")
        self.main_button.setObjectName("main_button")
        self.content_layout.addWidget(self.main_button, alignment=Qt.AlignmentFlag.AlignCenter)

        main_button_style = """
            #main_button {
                background-color: rgb(225, 235, 237);
                color: rgb(200, 65, 45);
                /* Force text alignment inside the button: */
                qproperty-alignment: 'AlignCenter';
            }
        """
        self.main_button.setStyleSheet(main_button_style)

        self.main_layout.addWidget(self.content_widget)

    def _side_bar_atr_(self):
        """
        Create a sidebar
        """
        self.sidebar_widget = QWidget()
        self.sidebar_layout = QVBoxLayout(self.sidebar_widget)

        for i in range(5):
            filler_button = QPushButton(f"Filler {i + 1}")
            filler_button.setObjectName("sidebar_button")
            self.sidebar_layout.addWidget(filler_button)

        self.sidebar_layout.addStretch()

        sidebar_style_sheet = """
            #sidebar_button {
                background-color: rgb(225, 235, 237);
                color: rgb(200, 65, 45);
                qproperty-alignment: 'AlignCenter';
            }
        """
        self.sidebar_widget.setStyleSheet(sidebar_style_sheet)

        self.main_layout.addWidget(self.sidebar_widget)



window = MainWindow()
window.show()
app.exec()

