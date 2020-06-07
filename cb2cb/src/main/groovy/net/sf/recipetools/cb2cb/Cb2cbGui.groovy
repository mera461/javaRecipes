package net.sf.recipetools.cb2cb; 

import groovy.beans.Bindable
import groovy.jface.JFaceBuilder
import groovy.swt.SwtBuilder

import org.eclipse.swt.widgets.DirectoryDialog
import org.eclipse.swt.widgets.FileDialog
import java.util.Properties

// 
import org.codehaus.groovy.runtime.StringBufferWriter
import org.codehaus.groovy.runtime.InvokerHelper

class Cb2cbGui {
    static String VERSION = '0.93'
    static String TITLE = "cb2cb - version $VERSION"
    def jface
    def mainapp
    def wizardDialog1
    def wizardPage1
    def inputfile    
    def inputformat
    def outputfile
    def outputformat
    def files
    boolean isOutputChangedByUser = false
    
    @Bindable
    boolean isFile = true
    
    @Bindable
    def mergeFiles = false
    
    def run() {
        jface = new JFaceBuilder()

        mainapp = jface.shell(TITLE, size:[10,10], minimized: true) {     
            wizardDialog1 = wizardDialog(TITLE, performFinish:{onPerformFinish();return true}) {
                wizardPage1 = wizardPage( title:'Input/Output', description:'Input and output files', closure: { parent ->
                    jface.composite( parent ) {
                        migLayout( layoutConstraints:"wrap 1", columnConstraints: "grow")
                        composite {
                            fillLayout()
                            label('Convert: ') 
                        	button(id:'isFileButton', 'Single File', style:'RADIO', selection: bind (model: this, modelProperty: 'isFile'))
                        	button(id:'isDirButton', 'Directory', style:'RADIO')
                        }

                        group('Input:', layoutData: 'growx') {
                            migLayout( layoutConstraints:"wrap 2", columnConstraints: "[][grow]")
                        	// the input format
                            label('Format:' )
                            inputformat = combo(style: 'READ_ONLY', items:['CRB', 'DVO', 'FDX', 'FDXZ', 'GCF', 'JSON', 'MGOURMET', 'MMF', 'MC2', 'MX2', 'MXP', 'MZ2', 'NYC', 'PAPRIKA', 'RPW', 'TXT'], layoutData:'growx')
                            label('File:' )
                        	// the input file
                        	inputfile = text(layoutData:"growx, split 2" ) {
                        		onEvent ('Modify') {
                        			if (! isOutputChangedByUser && inputfile) {
                        				outputfile.text = inputfile.text.replaceAll(/\.[^\.]*$/, '.'+outputformat.text)
                        			}
                        			if (new File(inputfile.text).canRead()) {
                        				wizardPage1.setPageComplete(true)
                        			}
                        		}
                        	}
                        	button( 'Browse...') {
                        		onEvent( 'Selection') { 
                        			def file = browseForFileOrDir(parent)
                        			if (file) {
                        				inputfile.setText(file)
                        			}
                        		}
                        	}
                            def m = this
                            button('Merge all files', style:'CHECK',
                            	   selection: bind (model:this, modelProperty: 'mergeFiles'), 
                                   enabled: bind {! m.isFile})
                        }
                        
                        // find the templates
                        def templates = []
                        def root = new File('templates') 
                        root.eachFileRecurse { file ->
                            if (file.name.toLowerCase().endsWith('.template')) {
                                templates << (file.absolutePath - root.absolutePath - '\\').toLowerCase()
                            }
                        }
                        
                        group ('Output:', layoutData: 'growx') {
                            migLayout( layoutConstraints:"wrap 2", columnConstraints: "[][grow]")
                            // the output file
                        	label( 'Format:' )
                        	outputformat = combo(style: 'READ_ONLY', items:['FDX', 'JSON', 'MGOURMET', 'MMF', 'MX2', 'MXP', 'MZ2', 'TXT']+templates, layoutData:'growx, wrap') {
                        		onEvent('Selection') {
                        			if (! isOutputChangedByUser && ! Character.isLowerCase(outputformat.text.charAt(0))) {
                        				outputfile.text = inputfile.text.replaceAll(/\.[^\.]*$/, '.'+outputformat.text)
                        			}
                        		}
                        	}
                            label( 'File:' )
                            outputfile = text(layoutData:"growx, split 2") {
                        		onEvent ('KeyDown') { isOutputChangedByUser=true }
                        	}
                        
                        	button( 'Browse...') {
                        		onEvent( 'Selection'){
                        			def file = browseForFileOrDir(parent)
                        			if (file) {
                        				outputfile.text = file
                        				isOutputChangedByUser=true
                        			}
                        		}
                        	}
                        }

						group('Options:', layoutData: 'growx') {
							gridLayout( numColumns:1 )
							button( text:'Change the configurations here.' ) {
								onEvent('Selection') { openPreferences() }
							}
						}
						
						// load the default input/output file
                        def props = new Properties();
                        try {
                            new File('cb2cb.ini').withInputStream { is ->
                            	props.load(is)
                            }
                            inputfile.text = props.getProperty('INPUTFILE', '')
                            inputformat.text = props.getProperty('INPUTFORMAT', 'TXT')
                            outputfile.text = props.getProperty('OUTPUTFILE', '')
                            outputformat.text = props.getProperty('OUTPUTFORMAT', 'MX2')
                            setIsFile(props.getProperty('INPUT_IS_FILE', 'true').toBoolean())
                            isFileButton.selection = isFile
                            isDirButton.selection = ! isFile
                            setMergeFiles(props.getProperty('INPUT_MERGE', 'false').toBoolean())
                        } catch (Exception e) {
                            // just ignore it. Dont set any default files. 
                            outputformat.text = 'MX2'
                        }
                    } 
                })

/*****				
                wizardPage( title:'Configuration', description:'Configuration', closure: { parent ->
                    jface.composite( parent ) {
                        gridLayout( numColumns:1 )
                        button( text:'Change the configurations here.' ) {
                            onEvent('Selection') { openPreferences() }
                        }
                    }
                })
******/                
            }
        }
        
        // start the wizard
        mainapp.open()
        wizardPage1.setPageComplete(false)
        wizardDialog1.open()
        
        return files
    }

    def browseForFileOrDir(def parent) {
        def file = ''
        if (isFile) {
        	file = new FileDialog(parent.getShell()).open()
        } else {
        	file = new DirectoryDialog(parent.getShell()).open()
        }
        return file
    }
    
    void onPerformFinish() {
        // save the default input/output file
        def props = new Properties();
        def is = new File('cb2cb.ini').newInputStream()
        props.load(is)
        is.close()
        props.setProperty('INPUTFILE', inputfile.getText())
        props.setProperty('INPUTFORMAT', inputformat.getText())
        props.setProperty('INPUT_IS_FILE', isFile.toString())
        props.setProperty('INPUT_MERGE', mergeFiles.toString())
        props.setProperty('OUTPUTFILE', outputfile.getText())
        props.setProperty('OUTPUTFORMAT', outputformat.getText())
        new File('cb2cb.ini').withOutputStream { os ->
        	props.store(os, 'This is the configuration file for cb2cb')
        }
        files = []
        if (! isFile && mergeFiles) files << '-m'
        files += ['-i',inputformat.text, '-o',outputformat.text, inputfile.text.trim(), outputfile.text.trim()]
    }
    
    void openPreferences() {
        def prefdialog = jface.preferenceDialog(mainapp) {
            preferencePage( title:'General', filename:'cb2cb.ini' ) { 
                
                radioGroupFieldEditor(propertyName:'TITLE_CASE_PREFERENCE', title:'Title casing', numColumns: 3, useGroup: true,
                labelAndValues: [['No change', '0'], ['Title Case', '1'], ['ALL UPPERCASE', '2']])
                radioGroupFieldEditor(propertyName:'SUBTITLE_CASE_PREFERENCE', title:'Subtitle casing', numColumns: 3, useGroup: true,
                        labelAndValues: [['No change', '0'], ['Title Case', '1'], ['ALL UPPERCASE', '2']])
                
                booleanFieldEditor( propertyName:'DETECT_AND_MARK_SERVINGS_FROM_YIELD', title:'If Yield contains servings then move it to the Serves field' )
                fileFieldEditor( propertyName:'MASTERCOOK_PROGRAM', title:'Path to Mastercook.exe' )
                directoryFieldEditor( propertyName:'LEADCMP_DIRECTORY', title:'Directory where LEADCMD is installed' )
            }
            preferencePage( title:'Amounts', filename:'cb2cb.ini' ) { 
                booleanFieldEditor( propertyName:'USE_FRACTIONS_IN_AMOUNTS', title:'Use Fractions in Amounts' )
            }
            preferencePage( title:'Units', filename:'cb2cb.ini' ) { 
                booleanFieldEditor( propertyName:'EXPAND_UNIT_ABBREVIATIONS', title:'Expand abbreviated units (�oz.� to �ounce� or �ounces�)' )
                booleanFieldEditor( propertyName:'CALCULATE_WEIGHT_FROM_AMOUNT_TIMES_PACKAGE_SIZE', title:'Calculate weight from amount X package size (�4 (10-ounce) jars� to �40 ounces�)' )
                booleanFieldEditor( propertyName:'USE_WEIGHT_NOT_PACKAGE_SIZE', title:'Use weight, not package size (�1 can sugar (about 150 g)� to �150 g sugar�)' )
                booleanFieldEditor( propertyName:'PLURALISE_UNITS', title:'Pluralize units ( �6 can� to �6 cans�, �4 box� to �4 boxes�)' )
            }
            preferencePage( title:'Ingredients', filename:'cb2cb.ini' ) { 
                booleanFieldEditor( propertyName:'MERGE_INGREDIENT_LINE_CONTINUATIONS', title:'Merge ingredient line continuations (remove ingredient line continuation marks �--� and merge into one line)' )
                booleanFieldEditor( propertyName:'SPLIT_INGREDIENT_LINES_WITH_PLUS', title:'Split ingredient lines with plus (text after plus goes on next line)' )
                booleanFieldEditor( propertyName:'MARK_ALTERNATE_INGREDIENT_LINES_AS_TEXT', title:'Mark alternate ingredient line as text (mark ingredient line after �or� as a text line)' )
                booleanFieldEditor( propertyName:'MARK_INGREDIENTS_WITH_NO_AMOUNT_OR_UNIT_AS_TEXT', title:'Mark ingredient lines with no units or amount as text lines' )
                booleanFieldEditor( propertyName:'INGREDIENT_PROCESSING_WORDS_TO_PREPARATION', title:'Move ingredient processing words to preparation field.' )
                booleanFieldEditor( propertyName:'DETECT_AND_MARK_SUBTITLES', title:'Detect and mark subtitles in ingredient rows' )
                booleanFieldEditor( propertyName:'MOVE_PARENTHESIS_IN_INGREDIENTS_TO_PREPARATION', title:'Move ingredient items in parentheses to preparation field' )
                booleanFieldEditor( propertyName:'MOVE_OPTIONAL_TO_PREPARATION', title:'Move the text "optional" to preparation field' )
                booleanFieldEditor( propertyName:'MOVE_TO_TASTE_TO_PREPARATION', title:'Move the text "to taste" to preparation field' )
                booleanFieldEditor( propertyName:'MOVE_TEXT_AFTER_COMMAS_TO_PREPARATION', title:'Move ingredient text after commas to preparation field (but obey commas.txt)' )
                booleanFieldEditor( propertyName:'MOVE_TEXT_AFTER_SEMICOLON_TO_PREPARATION', title:'Move ingredient text after semicolon to preparation field' )
                booleanFieldEditor( propertyName:'MOVE_SMALL_MED_LARGE_TO_INGREDIENTS', title:'Move the units small/medium/large to the start of the ingredient name' )
                booleanFieldEditor( propertyName:'CAPITALIZE_INGREDIENTS_WITHOUT_AMOUNT_UNIT', title:'Capitalize ingredient without amount and unit' )
            }
            preferencePage( title:'Directions', filename:'cb2cb.ini' ) { 
                booleanFieldEditor( propertyName:'DETECT_AND_MARK_SERVINGS_FROM_DIRECTIONS', title:'Detect and mark Serves from Directions' )
                booleanFieldEditor( propertyName:'MOVE_NOTES_IN_DIRECTIONS_TO_NOTES', title:'Detect and mark Notes from Directions' )
                booleanFieldEditor( propertyName:'REMOVE_NON_RECIPE_COMMENTS_FROM_DIRECTIONS', title:'Remove non-recipe comments from Directions (Formatted by, emails, etc.)' )
                booleanFieldEditor( propertyName:'REMOVE_NUTRITION_INFORMATION_FROM_DIRECTIONS', title:'Remove nutritional information from Directions' )
                booleanFieldEditor( propertyName:'REMOVE_INCORRECT_LINE_BREAKS_FROM_DIRECTIONS', title:'Remove line breaks in Directions/Notes' )
                booleanFieldEditor( propertyName:'REMOVE_DIRECTION_STEP_NUMBERS', title:'Remove Direction step numbers from Directions' )
            }
        }
        prefdialog.open()
    }
    
    public static void main(String[] args) {
        new Cb2cbGui().run();
    }
}
