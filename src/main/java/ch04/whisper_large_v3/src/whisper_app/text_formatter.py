from typing import Optional

def format_transcript_text(text: str,
                           line_width: Optional[int] = None,
                           preserve_paragraphs: bool = True) -> str:
    """
    Formats transcription text with proper line breaks for readability.

    This function takes raw transcription text and formats it by:
    1. Splitting text into sentences based on punctuation (.!?)
    2. Optionally wrapping long lines to a specified width
    3. Preserving paragraph structure for better readability

    Args:
        text (str): Raw transcription text to format
        line_width (int, optional): Maximum characters per line.
                                   If None, only split by sentences.
        preserve_paragraphs (bool): If True, adds blank lines between
                                   logical paragraph breaks.

    Returns:
        str: Formatted text with proper line breaks

    Example:
        >>> raw_text = "Hello world. This is a test."
        >>> format_transcript_text(raw_text)
        "Hello world.\nThis is a test."
    """
    # Validate input
    if not text or not isinstance(text, str):
        return text

    # 1. Split text into sentences using regex Pattern matches spaces after sentence-ending punctuation
    sentence_end_pattern = r'(?<=[.!?])\s+'
    sentences = re.split(sentence_end_pattern, text)

    # 2. Clean sentences (remove extra whitespace, filter empty)
    cleaned_sentences = [s.strip() for s in sentences if s.strip()]

    # 3. Apply line wrapping if width is specified
    if line_width and line_width > 0:
        wrapped_sentences = []
        for sentence in cleaned_sentences:
            # Wrap long sentences to specified width
            if len(sentence) > line_width:
                wrapped = textwrap.wrap(sentence, width=line_width)
                wrapped_sentences.extend(wrapped)
            else:
                wrapped_sentences.append(sentence)
        cleaned_sentences = wrapped_sentences

    # 4. Join sentences with appropriate separators
    if preserve_paragraphs:
        # Add paragraph breaks for natural reading flow
        formatted_lines = []
        for i, sentence in enumerate(cleaned_sentences):
            formatted_lines.append(sentence)
            # Add blank line after every 3-5 sentences for paragraph breaks
            if (i + 1) % 4 == 0 and i < len(cleaned_sentences) - 1:
                formatted_lines.append('')
        return '\n'.join(formatted_lines)
    else:
        # Simple line break after each sentence
        return '\n'.join(cleaned_sentences)


import re
import textwrap
from typing import Optional

def format_transcript_text(text: str,
                           line_width: Optional[int] = None,
                           preserve_paragraphs: bool = True) -> str:
    """
    Formats transcription text with proper line breaks for readability.

    This function takes raw transcription text and formats it by:
    1. Splitting text into sentences based on punctuation (.!?)
    2. Optionally wrapping long lines to a specified width
    3. Preserving paragraph structure for better readability

    Args:
        text (str): Raw transcription text to format
        line_width (int, optional): Maximum characters per line.
                                   If None, only split by sentences.
        preserve_paragraphs (bool): If True, adds blank lines between
                                   logical paragraph breaks.

    Returns:
        str: Formatted text with proper line breaks

    Example:
        >>> raw_text = "Hello world. This is a test."
        >>> format_transcript_text(raw_text)
        "Hello world.\nThis is a test."
    """
    # Validate input
    if not text or not isinstance(text, str):
        return text

    # 1. Split text into sentences using regex
    # Pattern matches spaces after sentence-ending punctuation
    sentence_end_pattern = r'(?<=[.!?])\s+'
    sentences = re.split(sentence_end_pattern, text)

    # 2. Clean sentences (remove extra whitespace, filter empty)
    cleaned_sentences = [s.strip() for s in sentences if s.strip()]

    # 3. Apply line wrapping if width is specified
    if line_width and line_width > 0:
        wrapped_sentences = []
        for sentence in cleaned_sentences:
            # Wrap long sentences to specified width
            if len(sentence) > line_width:
                wrapped = textwrap.wrap(sentence, width=line_width)
                wrapped_sentences.extend(wrapped)
            else:
                wrapped_sentences.append(sentence)
        cleaned_sentences = wrapped_sentences

    # 4. Join sentences with appropriate separators
    if preserve_paragraphs:
        # Add paragraph breaks for natural reading flow
        formatted_lines = []
        for i, sentence in enumerate(cleaned_sentences):
            formatted_lines.append(sentence)
            # Add blank line after every 3-5 sentences for paragraph breaks
            if (i + 1) % 4 == 0 and i < len(cleaned_sentences) - 1:
                formatted_lines.append('')
        return '\n'.join(formatted_lines)
    else:
        # Simple line break after each sentence
        return '\n'.join(cleaned_sentences)


def format_podcast_transcript(text: str, speaker_detection: bool = False) -> str:
    """
    Specialized formatting for podcast transcripts.

    Podcast transcripts often have specific structure:
    - Host introductions
    - Speaker changes
    - Narrative sections

    Args:
        text (str): Raw podcast transcription
        speaker_detection (bool): Attempt to detect speaker changes
                                (experimental, uses keyword matching)

    Returns:
        str: Formatted podcast transcript
    """
    # Split into sentences
    sentences = re.split(r'(?<=[.!?])\s+', text)

    formatted_lines = []
    previous_sentence = ""

    for i, sentence in enumerate(sentences):
        sentence = sentence.strip()
        if not sentence:
            continue

        # Experimental speaker change detection
        if speaker_detection and i > 0:
            # Keywords that might indicate speaker change
            speaker_keywords = [
                "I'm ", "I am ", "Host:", "Guest:",
                "Narrator:", "Interviewer:", "Interviewee:"
            ]

            # Check if this sentence starts like a new speaker
            is_new_speaker = any(
                sentence.lower().startswith(keyword.lower())
                for keyword in speaker_keywords
            )

            # Check if previous sentence was short (possible cue)
            was_short_previous = len(previous_sentence.split()) < 5

            if is_new_speaker or was_short_previous:
                formatted_lines.append('')  # Blank line before new speaker

        formatted_lines.append(sentence)
        previous_sentence = sentence

    return '\n'.join(formatted_lines)


def save_formatted_transcription(text: str, output_file: str, metadata: str = ""):
    """
    Saves formatted transcription to a file with metadata.

    This function handles the complete process of:
    1. Formatting the raw transcription text
    2. Writing metadata header
    3. Saving to specified output file

    Args:
        text (str): Raw transcription text from Whisper
        output_file (str): Path to output text file
        metadata (str, optional): Metadata header information
    """
    # Format the transcription text
    formatted_text = format_transcript_text(
        text,
        line_width=80,  # Standard terminal width
        preserve_paragraphs=True
    )

    # Write to file
    with open(output_file, 'w', encoding='utf-8') as f:
        if metadata:
            f.write(metadata)
        f.write(formatted_text)

    print(f"✓ Formatted transcript saved to: {output_file}")
    print(f"  Original length: {len(text)} characters")
    print(f"  Formatted length: {len(formatted_text)} characters")
    print(f"  Number of lines: {formatted_text.count(chr(10)) + 1}")


# Quick test function
def test_formatting():
    """Test the formatting functions with sample text."""
    sample_text = (
        "From WBEZ Chicago, it's This American Life. "
        "I'm Bim Adewunmi, in for Ira Glass. "
        "My younger cousin Camille is not really a dog person, "
        "but there is one dog she adored. "
        "Her name was Foxy, because she looked exactly like a fox, "
        "except she was black. "
        "She was the neighbor's dog, but she and Camille seemed to have "
        "a real kinship, maybe because they both weren't very far "
        "from the ground. "
        "Camille was around four or five years old back then, "
        "and she had a wonderful time playing with Foxy."
    )

    print("Original text preview:")
    print(sample_text[:200] + "...")
    print("\n" + "=" * 60 + "\n")

    print("Formatted with sentence breaks:")
    formatted = format_transcript_text(sample_text, line_width=None)
    print(formatted[:300] + "...")
    print("\n" + "=" * 60 + "\n")

    print("Formatted with 80-character width limit:")
    formatted_wrapped = format_transcript_text(sample_text, line_width=80)
    print(formatted_wrapped[:400] + "...")


if __name__ == "__main__":
    # Run test if script is executed directly
    test_formatting()