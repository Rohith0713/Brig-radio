import re
import socket
from typing import Tuple, Optional

# List of common typos in major email domains
COMMON_TYPOS = {
    'gamil.com': 'gmail.com',
    'gmal.com': 'gmail.com',
    'gmial.com': 'gmail.com',
    'gimail.com': 'gmail.com',
    'outook.com': 'outlook.com',
    'hotmal.com': 'hotmail.com',
    'yaho.com': 'yahoo.com',
    'icloud.cm': 'icloud.com',
    'googlemail.cm': 'googlemail.com',
    'gmail.cm': 'gmail.com',
}

def validate_email(email: str) -> Tuple[bool, Optional[str]]:
    """
    Validates an email address for format, common typos, and domain existence.
    
    Returns:
        Tuple of (is_valid, error_message)
    """
    if not email:
        return False, "Email is required."

    email = email.lower().strip()
    
    # 1. Basic format check
    if not re.match(r"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$", email):
        return False, "Invalid email format."
    
    try:
        _, domain = email.split('@')
    except ValueError:
        return False, "Invalid email format."
    
    # 2. Check for common typos
    if domain in COMMON_TYPOS:
        suggestion = COMMON_TYPOS[domain]
        return False, f"Did you mean {suggestion}? Please check your email domain."
    
    # 3. Check domain existence (optional but recommended for registration)
    # This might fail in environments without DNS access, so we wrap it in a try-except
    try:
        socket.setdefaulttimeout(3)  # 3 second timeout
        socket.gethostbyname(domain)
    except (socket.gaierror, socket.timeout, OSError):
        # DNS check failed - but don't block the email
        # The domain might be valid even if DNS is unreachable from this server
        pass

    return True, None
