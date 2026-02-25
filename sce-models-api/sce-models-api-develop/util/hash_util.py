from pathlib import Path
import hashlib
from typing import Union

def sha256_file(file_path: Union[str, Path], chunk_size: int = 1024 * 1024) -> str:
    """
    Calcula el SHA256 de un archivo.
    Acepta str o pathlib.Path.
    """
    path = Path(file_path)

    sha256 = hashlib.sha256()
    with path.open("rb") as f:
        for chunk in iter(lambda: f.read(chunk_size), b""):
            sha256.update(chunk)

    return sha256.hexdigest()
