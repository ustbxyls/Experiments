   M S B A M L   `   `   `  ����   WpfTextView    WpfTextView.FileView   \ XPresentationFramework, Version=4.0.0.0, Culture=neutral, PublicKeyToken=31bf3856ad364e35R NWindowsBase, Version=4.0.0.0, Culture=neutral, PublicKeyToken=31bf3856ad364e35W SPresentationCore, Version=4.0.0.0, Culture=neutral, PublicKeyToken=31bf3856ad364e35D 9http://schemas.microsoft.com/winfx/2006/xaml/presentation    R NSystem.Xaml, Version=4.0.0.0, Culture=neutral, PublicKeyToken=b77a5c561934e089:x,http://schemas.microsoft.com/winfx/2006/xaml     Jmc;http://schemas.openxmlformats.org/markup-compatibility/2006     @d2http://schemas.microsoft.com/expression/blend/2008     -   .���   � RowDefinitions  �� $��* ��� $
��Auto ��� -    �Name$ FindRow��$��0 �.+��� -   $��textView��$��0��$ �� IsSynchronizedWithCurrentItem$
 TrueY�$��Stretch=�$��Stretch=�$
��Auto��$��0,0,0,0q� �� SelectionMode$ Single=�$��Courier New)�����  �� Mode$ OneWay=� �� Path$ Contents��� -   $��splitter�� �� ResizeDirection$
 Rows=�$��1��$
��Auto��$��Stretch=�$��3��$��Stretch=�$��0q��� -   $��findResults��$��2��$��Stretch=�$
��Auto��$��Stretch=�$
��Auto��$��0,0,0,0q��� -   $��
findButton��$
��Find��$��2��$��23��$
��Left=�$	��Top=�$��44�� �� 	IsDefault . {��� $ CanStartFind��� -   $��
stopButton��$
��Stop��$��23��$
��Left=� .  $��50,0,0,0q�$	��Top=�$��44��$��2��{��� $ FindIsInProgress��� -   $��findText��$��2��$��23��$	��Top=�$��Stretch=�$��100,0,109,0q��� -	   $��	listView1��$��2��$��0,29,0,0q�$��Stretch=�$��Stretch=�$
��Auto��$ Single=����� $ FindResults� �� View �� .P��� $
��Line��	 �� Width$	 80��
 �� DisplayMemberBinding
 �� *
LineNumber+�� $��Contents��$
	 1600��
 �� *Line+� -
   $��findPercent��$��2��$��23��$��Right=�$��	700,0,0,0q� � Maximum$	 100Q�$	��Top=�$	��100�� � Value �� $ FindPercent�