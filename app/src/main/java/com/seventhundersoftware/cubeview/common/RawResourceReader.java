package com.seventhundersoftware.cubeview.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.content.Context;

/***
 This Java code is modified from
 https://github.com/learnopengles/Learn-OpenGLES-Tutorials
 code posted on GitHub.
 Their WebGL tutorials can be found at
 http://www.learnopengles.com.

 The RawResourceHeader class
 reads text from raw resources.
 Usually used to load vertex
 and fragment shaders.
*/
public class RawResourceReader
{
	public static String readTextFileFromRawResource(final Context context,
			final int resourceId)
	{
		final InputStream inputStream = context.getResources().openRawResource(
				resourceId);
		final InputStreamReader inputStreamReader = new InputStreamReader(
				inputStream);
		final BufferedReader bufferedReader = new BufferedReader(
				inputStreamReader);

		String nextLine;
		final StringBuilder body = new StringBuilder();

		try
		{
			while ((nextLine = bufferedReader.readLine()) != null)
			{
				body.append(nextLine);
				body.append('\n');
			}
		}
		catch (IOException e)
		{
			return null;
		}

		return body.toString();
	}
}
