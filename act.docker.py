import os

command = """
act push\
    --secret-file .env.act
"""

os.system(command.strip())
